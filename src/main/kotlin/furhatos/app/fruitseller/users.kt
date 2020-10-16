package furhatos.app.fruitseller

/*
import furhatos.app.fruitseller.nlu.FruitList
*/
import furhatos.records.User
import furhatos.records.Record
import furhatos.nlu.common.Number

class UserData (
        var orderNumber : Number? = Number(0),
        var name : String? = null,
        var problem : String? = null
)


val User.book : UserData
    get() = data.getOrPut(UserData::class.qualifiedName, UserData())
