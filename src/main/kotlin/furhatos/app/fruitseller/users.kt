package furhatos.app.fruitseller

/*
import furhatos.app.fruitseller.nlu.FruitList
*/
import furhatos.records.User
import furhatos.nlu.common.Number

class BookingData (
        var people : Number? = Number(0),
        var name : String? = null,
        var lengthStay : Number? = Number(0),
        var lengthType : String? = null,
        var roomClass : String? = null

)


val User.book : BookingData
    get() = data.getOrPut(BookingData::class.qualifiedName, BookingData())
