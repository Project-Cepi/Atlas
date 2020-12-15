package world.cepi.atlas.world.loader

/** The way that the loader retrieves and stores data */
enum class LoaderType {

    /** Handles data via Minestom storeage*/
    MINESTOM,

    /** Handles data in the physical file system */
    PHYSICAL

}