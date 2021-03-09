package com.example.demo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class WeatherObject(val description:String,val temperatureCelsius:String ){

}
