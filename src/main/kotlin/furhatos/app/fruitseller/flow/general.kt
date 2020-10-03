package furhatos.app.fruitseller.flow

import furhatos.flow.kotlin.furhat
import furhatos.flow.kotlin.onUserLeave
import furhatos.flow.kotlin.state

import furhatos.flow.kotlin.*
import furhatos.nlu.common.Goodbye
import furhatos.skills.UserManager
import furhatos.util.Language
import furhatos.records.Location

val Idle : State = state {
    /*
        On the first run only, if we have users in interaction
        space, we attend a random user and start the interaction.
        If not, we simply wait for a user to enter.

        If we return to this state, we attend nobody and wait for
        users to enter.
     */
    init {
        furhat.setTexture("male")
        furhat.setVoice(Language.ENGLISH_US, "Matthew")
        if (users.count > 0) {
            furhat.attend(users.random)
            goto(Start)
            //goto(FurtherDetails)
//            goto(SpecificWishes)
        }
    }

    onEntry {
        if (users.count > 0) {
            furhat.attendNobody()
        }
    }

    onUserEnter {
        var location = Location(80,80,100); // default gaze = x (looking away)
        furhat.attend(location)
        goto(Start)
        //goto(FurtherDetails)
    }
}

val Interaction : State = state {
    /*
        Generic state to inherit for states where we are
        attending a user.

        If an attended user leaves, the system either
        attends another user if existing or goes back to Idle.

        If a user enters, we glance at the user.
     */

    onUserLeave(instant = true) {
        if (users.count > 0) {
            if (it == users.current) {
                furhat.attend(users.other)
                goto(Start)
            } else {
                furhat.glance(it)
            }
        } else {
            goto(Idle)
        }
    }

    onUserEnter(instant = true) {
        furhat.glance(it)
    }

}

fun Furhat.askGlance(text : String) {
    attend(Location(80,80,100))
    glance(users.current, 1000)
    glance(users.other, 1000)
    glance(users.current, 1000)
    glance(users.other, 1000)
    attend(Location(-80,-80,0))
    ask(text)
}
