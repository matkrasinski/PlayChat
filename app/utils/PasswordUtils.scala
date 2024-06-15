package utils

import org.mindrot.jbcrypt.BCrypt


object PasswordUtils {
  def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt())
  }

  def checkPassword(candidate: String, hashed: String): Boolean = {
    BCrypt.checkpw(candidate, hashed)
  }
}
