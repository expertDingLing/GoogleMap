package com.polarbear.map

import com.google.gson.JsonObject

class Route {
    var type: String = "FeatureCollection"
    var features: ArrayList<Feature>? = null

    override fun toString(): String {
        return "Route{" +
                "type='" + type + '\'' +
                ", features=" + features +
                '}'
    }
}

class Feature {
    var type: String = "Feature"
    var properties: JsonObject? = null
    var geometry: Geometry? = null

    override fun toString(): String {
        return "Feature{" +
                "type='" + type + '\'' +
                ", properties=" + properties +
                ", geometry=" + geometry +
                '}'
    }
}

class Geometry {
    var type: String? = null
    var coordinates: Any? = null

    override fun toString(): String {
        return "Geometry{" +
                "type='" + type + '\'' +
                ", coordinates=" + coordinates +
                '}'
    }
}
