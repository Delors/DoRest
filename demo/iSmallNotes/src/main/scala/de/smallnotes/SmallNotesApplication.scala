package de.smallnotes

import org.dorest.server.{MappedDirectory, HandlerFactory}
import org.dorest.server.jdk.JDKServer

class SmallNotesApplication

object SmallNotesApplication extends JDKServer(8182) with App {

    this register new HandlerFactory[Notes] {
        path {
            "/api/tags/" :: LongValue((v) => _.t_id = v) :: "/notes" :: Optional("/")
        }

        def create = new Notes
    }

    this register new HandlerFactory[Tag] {
        path {
            "/tags/" :: LongValue((v) => _.id = v)
        }

        def create = new Tag
    }

    this register new HandlerFactory[Tags] {
        path {
            "/api/tags"
        }

        def create(): Tags = new Tags
    }

    this register new HandlerFactory[MappedDirectory] {
        path {
            "/webapp" :: AnyPath((v) => _.path = v)
        }

        def create(): MappedDirectory = new MappedDirectory({

            val dir = System.getProperty("de.smallnotes.resources.webapp")
            if (!(dir eq null))
                dir
            else
                "src/main/resources/webapp"
        },true)
    }

    start()

}


















