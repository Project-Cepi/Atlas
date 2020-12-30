package world.cepi.atlas.world

/** Defines the way chunks are presented */
enum class ChunkType {

    /**
     * If the chunk is dynamic, it will be loaded
     * in memory and can be globally modified
     */
    DYNAMIC,

    /**
     * If the chunk is passed,
     * its passed straight to the player and can still be modified locally,
     * but players that haven't loaded those changes will not see them'
     */
    PASSED

}