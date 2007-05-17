package net.liftweb.util

/*                                                *\
 (c) 2007 WorldWide Conferencing, LLC
 Distributed under an Apache License
 http://www.apache.org/licenses/LICENSE-2.0
 \*                                                 */

import java.net.InetAddress
import java.util.Properties
import Helpers._

/**
 * Property management
 */
object Props {
  /**
   * Get the property value
   * @param name the property to get
   *
   * @return the value of the property if defined
   */
  def get(name: String): Option[String] = props.get(name)

  def apply(name: String): String = props(name)
  
  def getInt(name: String): int = toInt(props.get(name))
  def getInt(name: String, defVal: int): int = props.get(name).map(toInt(_)) getOrElse defVal
  def getLong(name: String): long = toLong(props.get(name))
  def getLong(name: String, defVal: long): long = props.get(name).map(toLong(_)) getOrElse defVal
  def getBool(name: String): boolean = toBoolean(props.get(name))
  def getBool(name: String, defVal: boolean): boolean = props.get(name).map(toBoolean(_)) getOrElse defVal
  def get(name: String, defVal: String) = props.get(name) getOrElse defVal
  
  def require(what: String*) = what.filter(!props.contains(_))
  
  def requireOrDie(what: String*) {
     require(what :_*).toList match {
       case Nil =>
       case bad => throw new Exception("The following required properties are not defined: "+bad.mkString(","))
     }
   }
  
  val propFileName = "lift.props"
  val fileName = "lift.props"

  val userName = System.getProperty("user.name")
  val hostName = InetAddress.getLocalHost.getHostName

  private val toTry: List[() => String] = List(() => "/props/" + userName + "." + hostName + ".props",
					       () => "/props/" + userName + ".props",
					       () => "/props/" + hostName + ".props",
					       () => "/" + userName + "." + hostName + ".props",
					       () => "/" + userName + ".props",
					       () => "/" + hostName + ".props")
  val props = {
    // find the first property file that is available
    first(toTry)(f => tryo(getClass.getResourceAsStream(f()))).map{s => val ret = new Properties; ret.load(s); ret} match {
      case None => Map.empty[String, String] // if none, it's an empty map
      
      // if we've got a propety file, create name/value pairs and turn them into a Map
      case Some(prop) => 
        Map(prop.entrySet.toArray.map{
	s2 => 
	  val s = s2.asInstanceOf[java.util.Map.Entry]
	(s.getKey.asInstanceOf[String],s.getValue.asInstanceOf[String])
      } :_*)
    }
  }
}
