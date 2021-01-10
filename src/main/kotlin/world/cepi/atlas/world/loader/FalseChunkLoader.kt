package world.cepi.atlas.world.loader

import net.minestom.server.instance.Chunk
import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.Instance
import net.minestom.server.utils.chunk.ChunkCallback

/** It doesn't load chunks. */
class FalseChunkLoader(
    /** This is to make sure the constructor doesn't throw an exception. */
    @Suppress("UNUSED_PARAMETER") dummyPath: String
): IChunkLoader {
    override fun loadChunk(p0: Instance, p1: Int, p2: Int, p3: ChunkCallback?) = false

    override fun saveChunk(p0: Chunk, p1: Runnable?) = Unit

    override fun supportsParallelLoading() = true

    override fun supportsParallelSaving() = true
}