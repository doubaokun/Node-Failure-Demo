import org.json4s._
import org.json4s.JsonAST.{JString, JInt, JField, JObject}

import akka.actor._
import akka.serialization.{Serializer => AkkaSerializer}

package object mdl {



  class UrlSerializer extends Serializer[java.net.URL] {
    private val UrlClassClass = classOf[java.net.URL]

    def serialize(implicit formats: Formats): PartialFunction[Any, JValue] = {
      case x: java.net.URL =>
        import JsonDSL._
        x.toString //("url" -> x.toString) ~ ("blah" -> x.blah)
    }

    def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), java.net.URL] = {
      case (TypeInfo(UrlClassClass, _), json) => json match {
        case JString(url) => //JObject(JField("url", JString(id)) :: _) =>
          new java.net.URL(url)
        case x => throw new MappingException("Can't convert " + x + " to URL")
      }
    }
  }

  object ExternalAddress extends ExtensionKey[ExternalAddressExt]

  class ExternalAddressExt(system: ExtendedActorSystem) extends Extension {
    def addressForAkka: Address = system.provider.getDefaultAddress
    def actorForAddress(path : String) : ActorRef = system.provider.resolveActorRef(path)
  }
  
  class ShallowActorSerializer( actorSystem : ActorSystem) extends Serializer[ActorRef] {
    private val ActorRefClassClass = classOf[ActorRef]
    //implicit val actorSystem : ActorSystem

    override def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
      case a : ActorRef =>
        import JsonDSL._
        a.path.toSerializationFormatWithAddress( ExternalAddress(actorSystem).addressForAkka )
    }

    override def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), ActorRef] = {
      case (TypeInfo(ActorRefClassClass, _), json) => json match {
        case JString(address) =>
          ExternalAddress(actorSystem).actorForAddress(address)
        case x => throw new MappingException("Can't convert " + x + " to an Actor")
      }
    }
  }

}
