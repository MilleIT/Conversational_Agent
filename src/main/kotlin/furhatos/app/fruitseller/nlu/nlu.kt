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

class GiveName(var name: UserName? = null) : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("@name")
    }
}





