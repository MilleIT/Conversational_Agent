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

class SimpleNo() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("No", "Continue", "Continue please", "Let's move on", "No thanks", "Nope", "Not really", "Nahh", "I don't think so", "Rather not", "Let's just continue", "No I want to quickly fix this", "Not exactly", "Rather not")
    }
}

class SimpleYes() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Yes", "Elaborate", "Elaborate please", "Yes thanks", "Yes sure", "Alright", "Sure", "Ok", "Yeah", "Yeah ok", "Okay", "I will", "Yes can I start")
    }
}

class AvailableDays(var days : Days? = null) : ComplexEnumEntity() {
    override fun getEnum(lang: Language): List<String> {
        return listOf("@days")
    }
}

class Days : EnumEntity(stemming = true, speechRecPhrases = true) {
    override fun getEnum(lang: Language): List<String> {
        return listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    }
}

class Headphones() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Headphones")
    }
}
class Laptop() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Laptop")
    }
}
class Television() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Television", "TV")
    }
}
class Playstation() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Playstation")
    }
}

class NoDay() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("No day", "None", "I am not home this week")
    }
}

class WrongPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I got the wrong package", "Wrong package", "Wrong package delivered", "I received the wrong package", "The second one", "The second problem")
    }
}

class NoPackage() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("My package didn't arrive", "I didn't receive my package", "No package received", "No package", "I didn't receive the package",  "The package wasn't delivered", "My package isn't here", "The package isn't here", "The package is late", "The package is lost", "The first one", "The first problem")
    }
}

class NoRefund() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("I didn't receive my refund", "No refund", "No refund received", "I'm not refunded", "I didn't receive a refund", "The last one", "The last problem", "The third one", "The third problem")
    }
}

class OrderAndName(var ordernumber : Number? = null, var name : PersonName? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@ordernumber, @name", "My ordernumber is @ordernumber and my last name is @name")
    }
}

class OrderNumber(var ordernumber : Number? = null) : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("@ordernumber", "My ordernumber is @ordernumber")
    }
}

class Bad() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Bad", "Bed", "Poor", "Disappointing", "Unsatisfactory", "Hopeless", "Unacceptable", "Shit", "Crap", "Sucked", "Useless", "Terrible", "Awful", "Miserable")
    }
}

class Ok() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Ok", "Alright", "Not too bad", "Fine", "Average", "Okay", "Standard", "Fair", "Neutral")
    }
}

class Good() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Good", "Very good", "Nice", "Very nice", "Fantastic", "Excellent", "Super", "Glorious", "Impressive", "Outstanding", "Remarkable")
    }
}

class Cancel() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to cancel it", "I would like to cancel my order", "I want to cancel it", "I want to cancel my order", "cancel order", "cancel it", "don't want it anymore", "come pick it up")
    }
}

class StillReceive() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I'd like to receive it", "I would still like to receive it", "I would still like to receive my order", "I would like to receive my order", "I would like to receive my product", "please send it now", "send it now", "can you still deliver it", "do deliver it")
    }
}

class NotHome() : Intent(){
    override fun getExamples(lang: Language): List<String> {
        return listOf("I am not home tomorrow", "No not tomorrow", "I'm not home then")
    }
}

class TooLate() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("Return the order", "That last part", "Send it back", "That is too late, I don't want it anymore.", "Too late", "Don't want it", "Come pick it up", "Can I return it?", "I want to return it", "Is it possible to return it", "I have no use for it")
    }
}

class Confirm() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("No, that's alright", "A coupon is ok", "Yes, a coupon", "A coupon please", "Okay", "Alright", "Ok", "Thanks", "Thank you", "Fine", "Good", "Nice", "Yes", "Yes, please", "Aye", "Excellent", "Wonderful", "Superb", "Great", "Fine", "That's good", "That's nice", "That's great")
    }
}

class EmailOrText() : Intent() {
    override fun getExamples(lang: Language): List<String> {
        return listOf("email", "Text Message", "Whatsapp", "I would like be contacted via email")
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








