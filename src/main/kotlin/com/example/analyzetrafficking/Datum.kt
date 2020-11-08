package com.example.analyzetrafficking

import lombok.NonNull


data class Datum constructor(

    @NonNull
    var message: Int = 0,
    @NonNull
    var post: Int = 0
){
    init{ message = 0; post = 0}
}
