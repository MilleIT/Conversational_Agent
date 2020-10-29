package furhatos.app.fruitseller

/*
import furhatos.app.fruitseller.nlu.FruitList
*/
import furhatos.app.fruitseller.nlu.Days
import furhatos.flow.kotlin.UserDataDelegate
import furhatos.records.User
import furhatos.nlu.common.Number

class UserData (
        var orderNumber : Number? = Number(0),
        var name : String? = null,
        var problem : String? = null,
        var emotion: String? = null,
        var intendedOrder: String? = null,
        var receivedOrder: String? = null,
        var days : Days? = null
)

val User.book : UserData
    get() = data.getOrPut(UserData::class.qualifiedName, UserData())
