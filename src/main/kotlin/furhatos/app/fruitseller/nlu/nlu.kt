package furhatos.app.fruitseller.nlu

import furhatos.nlu.*
import furhatos.nlu.grammar.Grammar
import furhatos.nlu.kotlin.grammar
import furhatos.nlu.common.Number
import furhatos.nlu.common.PersonName
import furhatos.util.Language

class RequestOptions: Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("What options do you have?",
                "What fruits do you have?",
                "What are the alternatives?",
                "What do you have?")
    }
}

class PeopleList : ListEntity<QuantifiedPeople>()

class QuantifiedPeople(
        var count : Number? = Number(1),
        var person : Person? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @person", "@person")
    }

//    fun getCount(): Int {
//        return generate("$count").toInt()
//    }

    override fun toText(): String {
        return generate("${count?.value}")
    }
//    override fun toText(): String {
//        return generate("$count " + if (count?.value == 1) person?.value else "${person?.value}")
//    }
}

class Person : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("person", "people")
    }
}



//class BuyFruit(var fruits : FruitList? = null) : Intent() {
//    override fun getExamples(lang: Language): List<String> {
//        return listOf("@fruits", "I want @fruits", "I would like @fruits", "I want to buy @fruits")
//    }
//}

class IntendedOrder() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Headphone", "Laptop", "TV", "Playstation")
    }
}

class DaysInWeek() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }
}

class NoDay() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("No day")
    }
}

class WrongPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I got the wrong package")
    }
}

class NoPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("My package didn't arrive")
    }
}

class NoRefund() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I didn't receive my refund")
    }
}

class OrderAndName() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I didn't receive my refund") //MOET VARIABLE KUNNEN ACCEPTEN!
    }
}













class TellPeople(var people: PeopleList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@people")
    }
}

class StayList : ListEntity<QuantifiedStay>()

class QuantifiedStay(
        var count : Number? = Number(1),
        var stay : Staytype? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @stay")
    }

    override fun toText(): String {
        return generate("${count?.value} " + if (count?.value == 1) stay?.value else "${stay?.value}s")

    }
}
class Staytype : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("day", "days", "week", "weeks")
    }
}
class Roomtype : EnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("suite", "citizen")
    }
}

class GiveLengthStay(var stayL : StayList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@stayL")
    }
}

class GiveRoomClass(var room : Roomtype? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@room")
    }
}

class GiveName(var naam: UserName? = null) : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@naam")
    }
}

class UserName(var name : String? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@name")
    }

    override fun toText(): String {
        return generate("$name")
    }
}

class Details(var name: PersonName? = null, var stay: StayList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@name", "@stay", "suite", "citizen")
    }
}

class Wishes : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("extra blanket", "bottle of water", "room service")
    }
}

class WishList : ListEntity<QuantifiedWishes>()

class QuantifiedWishes(
        val count : Number? = Number(1),
        val wish : Wishes? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @wish", "@wish")
    }

    override fun toText(): String {
        return generate("$count $wish")
    }
}

class TellWish(val wish : WishList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@wish")
    }
}

class Activities : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Skiing", "Tennis", "Badminton", "Zombie Survival")
    }
}

class ActivitiesList : ListEntity<QuantifiedActivities>()

class QuantifiedActivities(
        val count : Number? = Number(1),
        val activity : Activities? = null) : ComplexEnumEntity() {

    override fun getEnum(lang: Language): List<String> {
        return listOf("@count @activity", "@activity")
    }

    override fun toText(): String {
        return generate("$count $activity")
    }
}

class listActivities(val activities : ActivitiesList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@activities")
    }
}








