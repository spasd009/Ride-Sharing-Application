package com.project;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
@SpringBootTest(properties={"spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:tests;MODE=MySQL;DB_CLOSE_DELAY=-1}","spring.datasource.driver-class-name=${TEST_DATABASE_DRIVER:org.h2.Driver}","spring.datasource.username=${TEST_DATABASE_USER:sa}","spring.datasource.password=${TEST_DATABASE_PASSWORD:}","spring.sql.init.schema-locations=${TEST_SCHEMA:classpath:test-schema.sql}"})
@AutoConfigureMockMvc
class ProjectIntegrationTest {
 @Autowired ProjectService service; @Autowired JdbcTemplate db; @Autowired MockMvc mvc;
 @BeforeEach void reset(){db.update("DELETE FROM bookings");db.update("DELETE FROM rides");db.update("DELETE FROM users");service.register("driver","secure-pass-123");service.register("alice","secure-pass-123");service.register("bob","secure-pass-123");}
 long ride(int seats){service.publish("driver","Mumbai","Pune",LocalDateTime.now().plusDays(1).withNano(0).toString(),new BigDecimal("800.00"),seats,"Honda City");return ((Number)service.published("driver").get(0).get("ID")).longValue();}
 @Test void publicPagesAndAuthenticatedPagesRender() throws Exception {
  for(String path:new String[]{"/","/rides","/login","/register"})mvc.perform(get(path)).andExpect(status().isOk());
  for(String path:new String[]{"/account","/rides/new"})mvc.perform(get(path).with(user("alice"))).andExpect(status().isOk());
  mvc.perform(get("/account")).andExpect(status().is3xxRedirection());
 }
 @Test void registrationLoginAndCsrf() throws Exception {
  mvc.perform(post("/register").param("username","charlie").param("password","long-password-123")).andExpect(status().isForbidden());
  mvc.perform(post("/register").with(csrf()).param("username","charlie").param("password","long-password-123")).andExpect(redirectedUrl("/login"));
  mvc.perform(post("/login").with(csrf()).param("username","charlie").param("password","long-password-123")).andExpect(redirectedUrl("/account"));
  String hash=db.queryForObject("SELECT password FROM users WHERE username='charlie'",String.class);assertNotEquals("long-password-123",hash);
 }
 @Test void bookingCancellationAndOwnership(){long id=ride(1);assertThrows(IllegalArgumentException.class,()->service.book("driver",id));service.book("alice",id);assertThrows(IllegalArgumentException.class,()->service.book("alice",id));assertThrows(IllegalArgumentException.class,()->service.book("bob",id));long booking=((Number)service.bookings("alice").get(0).get("BOOKING_ID")).longValue();assertThrows(IllegalArgumentException.class,()->service.cancel("bob",booking));service.cancel("alice",booking);assertEquals(1,db.queryForObject("SELECT seats FROM rides WHERE id=?",Integer.class,id));}
 @Test void concurrentBookingsCannotOversell() throws Exception {
  long id=ride(1);ExecutorService pool=Executors.newFixedThreadPool(2);CountDownLatch start=new CountDownLatch(1);
  try{Callable<Boolean> a=()->{start.await();try{service.book("alice",id);return true;}catch(IllegalArgumentException e){return false;}};Callable<Boolean> b=()->{start.await();try{service.book("bob",id);return true;}catch(IllegalArgumentException e){return false;}};Future<Boolean> x=pool.submit(a),y=pool.submit(b);start.countDown();assertNotEquals(x.get(10,TimeUnit.SECONDS),y.get(10,TimeUnit.SECONDS));assertEquals(0,db.queryForObject("SELECT seats FROM rides WHERE id=?",Integer.class,id));assertEquals(1,db.queryForObject("SELECT COUNT(*) FROM bookings",Integer.class));}finally{pool.shutdownNow();}
 }
 @Test void searchWatchlistAndValidation(){long id=ride(2);assertEquals(1,service.rides("mumbai","pune","").size());assertEquals(0,service.rides("delhi","pune","").size());assertThrows(IllegalArgumentException.class,()->service.publish("driver","Pune","Pune",LocalDateTime.now().plusDays(1).toString(),BigDecimal.ONE,1,"Car"));assertThrows(IllegalArgumentException.class,()->service.register("x","short"));}
 @Test void bookingAndErrorViewsRender() throws Exception {long id=ride(2);mvc.perform(post("/rides/"+id+"/book").with(user("alice")).with(csrf())).andExpect(redirectedUrl("/account"));mvc.perform(get("/account").with(user("alice"))).andExpect(status().isOk());mvc.perform(get("/rides").param("date","bad-date")).andExpect(status().isBadRequest());}
 @Test void privateDataAndSecurityHeaders() throws Exception {
  String body=mvc.perform(get("/account").with(user("alice")))
   .andExpect(status().isOk())
   .andExpect(header().string("X-Frame-Options","DENY"))
   .andExpect(header().string("X-Content-Type-Options","nosniff"))
   .andExpect(header().string("Referrer-Policy","no-referrer"))
   .andExpect(header().string("Permissions-Policy","camera=(), microphone=(), geolocation=()"))
   .andExpect(header().string("Content-Security-Policy",org.hamcrest.Matchers.containsString("frame-ancestors 'none'")))
   .andExpect(header().string("Cache-Control",org.hamcrest.Matchers.containsString("no-store")))
   .andReturn().getResponse().getContentAsString();
  String hash=db.queryForObject("SELECT password FROM users WHERE username='alice'",String.class);
  assertFalse(body.contains(hash));assertFalse(body.contains("secure-pass-123"));assertFalse(body.contains("jdbc:"));
  for(String path:new String[]{"/application.properties","/schema.sql","/pom.xml","/.env","/h2-console"})mvc.perform(get(path).with(user("alice"))).andExpect(status().isNotFound());
 }
 @Test void passwordByteLimitIsValidated(){assertThrows(UserInputException.class,()->service.register("unicode_user","é".repeat(40)));}

 @Test void publicRidesDoNotExposeLoginNamesOrOthersBookings() throws Exception {
  long id=ride(2);assertFalse(service.rides("","","").get(0).containsKey("DRIVER"));service.book("alice",id);
  assertTrue(service.bookings("bob").isEmpty());
  String publicBody=mvc.perform(get("/rides")).andReturn().getResponse().getContentAsString();assertFalse(publicBody.contains(">driver<"));
  String error=mvc.perform(get("/rides").param("date","PRIVATE_SENTINEL_NOT_A_DATE")).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();assertFalse(error.contains("PRIVATE_SENTINEL"));assertFalse(error.contains("DateTimeParseException"));
 }

}
