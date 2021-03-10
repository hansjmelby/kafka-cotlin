package com.example.demo

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class Book(val author:String,val title:String, val publisher:String,val genre:String,val price:Double)
