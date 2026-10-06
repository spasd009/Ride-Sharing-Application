package com.project;
/** An intentional, safe message for the user. Never include secrets or SQL here. */
public class UserInputException extends IllegalArgumentException {
 public UserInputException(String message){super(message);}
}
