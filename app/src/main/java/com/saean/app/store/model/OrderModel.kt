package com.saean.app.store.model

class OrderModel {
    var orderID : String? = null
    var orderTime : Long? = null
    var orderStore : String? = null
    var orderUser : String? = null
    var orderService : String? = null
    var orderDescription : String? = null
    var orderStatus : Int? = null
    var orderProcess : Int? = null
    var orderPicture : ArrayList<OrderPictureModel>? = null
}