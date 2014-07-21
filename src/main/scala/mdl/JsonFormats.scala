package mdl

import org.json4s.{NoTypeHints, native}

trait JsonFormats {

  val baseformats = native.Serialization.formats( NoTypeHints ) +
    //new org.json4s.ext.EnumNameSerializer(RoleSpec) +
    //new org.json4s.ext.EnumNameSerializer(MitigationAt) +
    //new org.json4s.ext.EnumNameSerializer(FailureRecovery) +
    new UrlSerializer //+
    //new AccessSerializer

}
