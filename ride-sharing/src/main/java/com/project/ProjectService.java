package com.project;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ProjectService {
 private final JdbcTemplate db; private final PasswordEncoder passwords;
 public ProjectService(JdbcTemplate db,PasswordEncoder passwords){this.db=db;this.passwords=passwords;}
 @Transactional public void register(String name,String password){
  if(!name.matches("[A-Za-z0-9_]{3,40}")||password.length()<10||password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length>72)throw new UserInputException("Use a username of 3–40 letters, digits or underscores and a password of 10–72 characters.");
  if(!db.queryForList("SELECT username FROM users WHERE username=?",name).isEmpty())throw new UserInputException("Username already exists.");
  db.update("INSERT INTO users VALUES (?,?)",name,passwords.encode(password));
 }
 public List<Map<String,Object>> rides(String from,String to,String date){
  LocalDate day=date.isBlank()?null:LocalDate.parse(date);
  String sql="SELECT id,origin,destination,departure,price,seats,car FROM rides WHERE seats>0 AND departure>CURRENT_TIMESTAMP AND LOWER(origin) LIKE ? AND LOWER(destination) LIKE ?";
  List<Object> args=new ArrayList<>(List.of("%"+from.toLowerCase(Locale.ROOT)+"%","%"+to.toLowerCase(Locale.ROOT)+"%"));
  if(day!=null){sql+=" AND departure>=? AND departure<?";args.add(day.atStartOfDay());args.add(day.plusDays(1).atStartOfDay());}return db.queryForList(sql+" ORDER BY departure",args.toArray());
 }
 @Transactional public void publish(String name,String from,String to,String departure,BigDecimal price,int seats,String car){
  from=from.trim();to=to.trim();car=car.trim();LocalDateTime time=LocalDateTime.parse(departure);
  if(from.isEmpty()||to.isEmpty()||car.isEmpty()||from.length()>100||to.length()>100||car.length()>100||from.equalsIgnoreCase(to))throw new UserInputException("Enter different departure and destination cities and a car model (maximum 100 characters each).");
  if(!time.isAfter(LocalDateTime.now())||time.isAfter(LocalDateTime.now().plusYears(1)))throw new UserInputException("Departure must be within the next year.");
  if(price.signum()<=0||price.compareTo(new BigDecimal("999999.99"))>0||price.scale()>2||seats<1||seats>6)throw new UserInputException("Enter a valid price and 1–6 seats.");
  db.update("INSERT INTO rides(driver,origin,destination,departure,price,seats,car) VALUES (?,?,?,?,?,?,?)",name,from,to,time,price,seats,car);
 }
 @Transactional public void book(String name,long id){
  var rows=db.queryForList("SELECT * FROM rides WHERE id=? FOR UPDATE",id);
  if(rows.isEmpty())throw new UserInputException("Ride not found.");var ride=rows.get(0);
  if(name.equals(ride.get("DRIVER")))throw new UserInputException("You cannot book your own ride.");
  if(!db.queryForList("SELECT id FROM bookings WHERE username=? AND ride_id=?",name,id).isEmpty())throw new UserInputException("You already booked this ride.");
  int changed=db.update("UPDATE rides SET seats=seats-1 WHERE id=? AND seats>0 AND departure>CURRENT_TIMESTAMP",id);
  if(changed!=1)throw new UserInputException("This ride is full or has departed.");
  db.update("INSERT INTO bookings(username,ride_id) VALUES (?,?)",name,id);
 }
 public List<Map<String,Object>> bookings(String name){return db.queryForList("SELECT r.id,r.origin,r.destination,r.departure,r.price,r.seats,r.car,b.id AS booking_id FROM bookings b JOIN rides r ON r.id=b.ride_id WHERE b.username=? ORDER BY r.departure",name);}
 public List<Map<String,Object>> published(String name){return db.queryForList("SELECT id,origin,destination,departure,price,seats,car FROM rides WHERE driver=? ORDER BY departure",name);}
 @Transactional public void cancel(String name,long id){
  var rows=db.queryForList("SELECT ride_id FROM bookings WHERE id=? AND username=?",id,name);
  if(rows.isEmpty())throw new UserInputException("Booking not found.");long ride=((Number)rows.get(0).get("RIDE_ID")).longValue();
  db.queryForList("SELECT id FROM rides WHERE id=? FOR UPDATE",ride);
  if(db.queryForList("SELECT id FROM rides WHERE id=? AND departure>CURRENT_TIMESTAMP",ride).isEmpty())throw new UserInputException("A departed ride cannot be cancelled.");
  if(db.update("DELETE FROM bookings WHERE id=? AND username=?",id,name)==1)db.update("UPDATE rides SET seats=seats+1 WHERE id=?",ride);
 }
}
