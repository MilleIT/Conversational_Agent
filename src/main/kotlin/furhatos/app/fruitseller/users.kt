package furhatos.app.fruitseller

/*
import furhatos.app.fruitseller.nlu.FruitList
*/
import furhatos.records.User
import furhatos.records.Record
import furhatos.nlu.common.Number

class BookingData (
        var people : Number? = Number(0),
        var name : String? = null,
        var lengthStay : String? = null,
        var roomClass : String? = null,
        var wish : String? = null,
        var suiteRooms : Number? = Number(4),
        var citiRooms : Number? = Number(8)
)

val User.book : BookingData
    get() = data.getOrPut(BookingData::class.qualifiedName, BookingData())