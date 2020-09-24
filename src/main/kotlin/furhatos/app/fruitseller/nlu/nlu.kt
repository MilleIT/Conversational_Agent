package furhatos.app.fruitseller.nlu

import furhatos.nlu.*
import furhatos.nlu.grammar.Grammar
import furhatos.nlu.kotlin.grammar
import furhatos.nlu.common.Number
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

class UserName(var name : String? = null) {
    override fun toString(): String {
        return "@name"
    }
}

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

class Staytype : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("day", "days", "week", "weeks")
    }
}

//class BuyFruit(var fruits : FruitList? = null) : Intent() {
//    override fun getExamples(lang: Language): List<String> {
//        return listOf("@fruits", "I want @fruits", "I would like @fruits", "I want to buy @fruits")
//    }
//}

class CheckIn() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I would like to check in")
    }
}

class Confusion() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Who are you?", "Where am I?", "What is this?", "What")
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
        return generate("${count?.value} " + "${stay?.value}")
    }
}
class GiveLengthStay(var stay: StayList? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@stay")
    }
}

class GiveRoomClass() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Suite", "Class")
    }
}

class GiveName(var name: UserName? = null) : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@name")
    }
}

class Wishes : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("extra blanket", "bottle of water", "Room service")
    }
}

class WishList : ListEntity<QuantifiedWishes>()

class QuantifiedWishes(
        val count : furhatos.nlu.common.Number? = Number(1),
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
        val count : furhatos.nlu.common.Number? = Number(1),
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








