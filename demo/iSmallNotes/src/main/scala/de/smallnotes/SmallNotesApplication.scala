package de.smallnotes

import org.dorest.server.{ MappedDirectory, HandlerFactory }
import org.dorest.server.jdk.JDKServer

class SmallNotesApplication

object SmallNotesApplication extends JDKServer(8182) with App {

    val rootWebappFolder = {
        var rwf = System.getProperty("de.smallnotes.resources.webapp")
        if (!(rwf eq null))
            rwf
        else
            "src/main/resources/webapp"
    }

    this addPathMatcher (
        / {
            case "api" ⇒ / {
                case "tags" ⇒ / {
                    case MATCHED() ⇒ new Tags
                    case LONG(tagId) ⇒ / {
                        case "notes" ⇒ new Notes(tagId)
                    }
                }
            }
            case "tags" ⇒ / {
                case LONG(id) ⇒ new Tag(id)
            }
            case "webapp" ⇒ (path) ⇒ Some(new MappedDirectory(rootWebappFolder, path, true))
        }
    )

    start()
}


















