package com.kovcom.mowid

import org.dynodict.DynoDict
import org.dynodict.model.StringKey

object App : StringKey("App") {
    object Name : StringKey("Name", App) {
        val value: String
            get() = DynoDict.instance.get(this)
    }
}
object Title : StringKey("Title") {
    object Home : StringKey("Home", Title) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Settings : StringKey("Settings", Title) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Add : StringKey("Add", Title) {
        object Group : StringKey("Group", Add) {

            val value: String
                get() = DynoDict.instance.get(this)
        }

        object Quote : StringKey("Quote", Add) {

            val value: String
                get() = DynoDict.instance.get(this)
        }
    }

    object Edit : StringKey("Edit", Title) {
        object Group : StringKey("Group", Edit) {

            val value: String
                get() = DynoDict.instance.get(this)
        }

        object Quote : StringKey("Quote", Edit) {

            val value: String
                get() = DynoDict.instance.get(this)
        }
    }
}
object Label : StringKey("Label") {
    object Add : StringKey("Add", Label) {
        val value: String
            get() = DynoDict.instance.get(this)
    }
    object Edit : StringKey("Edit", Label) {
        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Cancel : StringKey("Cancel", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Group : StringKey("Group", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Description : StringKey("Description", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Quote : StringKey("Quote", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Author : StringKey("Author", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Delete : StringKey("Delete", Label) {

        val value: String
            get() = DynoDict.instance.get(this)

        object Group : StringKey("Group", Delete) {

            val value: String
                get() = DynoDict.instance.get(this)

            object Message : StringKey("Message", Group) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }

        object Quote : StringKey("Quote", Delete) {

            val value: String
                get() = DynoDict.instance.get(this)

            object Message : StringKey("Message", Quote) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }
    }

    object Apply : StringKey("Apply", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Frequency : StringKey("Frequency", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Applied : StringKey("Applied", Label) {

        val value: String
            get() = DynoDict.instance.get(this)
    }

    object Empty : StringKey("Empty", Label) {
        object State : StringKey("State", Empty) {

            val value: String
                get() = DynoDict.instance.get(this)
        }
    }
    object Sign : StringKey("Sign", Label) {
        object In : StringKey("In", Sign) {

            val value: String
                get() = DynoDict.instance.get(this)

            object Success : StringKey("Success", In) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }

            object Error : StringKey("Error", In) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }

        object Out : StringKey("Out", Sign) {

            val value: String
                get() = DynoDict.instance.get(this)

            object Success : StringKey("Success", Out) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }
    }
    object User : StringKey("User", Label) {
        object Not : StringKey("Not", User) {
            object Registered : StringKey("Registered", Not) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }

        object Signed : StringKey("Signed", User) {
            object In : StringKey("In", Signed) {
                object As : StringKey("As", In) {

                    val value: String
                        get() = DynoDict.instance.get(this)
                }
            }
        }
    }
}
object Once : StringKey("Once") {
    object A : StringKey("A", Once) {
        object Week : StringKey("Week", A) {
            val value: String
                get() = DynoDict.instance.get(this)
        }
        object Day : StringKey("Day", A) {
            val value: String
                get() = DynoDict.instance.get(this)
        }
    }

    object In : StringKey("In", Once) {
        object Two : StringKey("Two", In) {
            object Days : StringKey("Days", Two) {

                val value: String
                    get() = DynoDict.instance.get(this)
            }
        }

        object A : StringKey("A", In) {
            object Five : StringKey("Five", A) {
                object Days : StringKey("Days", Five) {

                    val value: String
                        get() = DynoDict.instance.get(this)
                }
            }
        }
    }
}
object Twice : StringKey("Twice") {
    object A : StringKey("A", Twice) {
        object Day : StringKey("Day", A) {

            val value: String
                get() = DynoDict.instance.get(this)
        }
    }
}

object Fours : StringKey("Fours") {
    object A : StringKey("A", Fours) {
        object Day : StringKey("Day", A) {

            val value: String
                get() = DynoDict.instance.get(this)
        }
    }
}

object Sign : StringKey("Sign") {
    object In : StringKey("In", Sign) {
        object Alert : StringKey("Alert", In) {
            object Dialog : StringKey("Dialog", Alert) {
                object Text : StringKey("Text", Dialog) {

                    val value: String
                        get() = DynoDict.instance.get(this)
                }
            }
        }
    }
}
