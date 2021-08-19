package world.cepi.atlas.world.loader

import net.minestom.server.instance.Chunk
import net.minestom.server.instance.IChunkLoader
import net.minestom.server.instance.Instance
import net.minestom.server.utils.chunk.ChunkCallback
import org.jetbrains.annotations.NotNull
import org.jetbrains.annotations.Nullable
import java.nio.file.Path
import java.util.concurrent.CompletableFuture

/** It doesn't load chunks. */
class FalseChunkLoader(
    /** This is to make sure the constructor doesn't throw an exception. */
    @Suppress("UNUSED_PARAMETER") dummyPath: Path
): IChunkLoader {
    override fun loadChunk(p0: Instance, p1: Int, p2: Int): CompletableFuture<Chunk?> = CompletableFuture()

    override fun saveChunk(p0: Chunk): CompletableFuture<Void> = CompletableFuture()

    override fun supportsParallelLoading() = true

    override fun supportsParallelSaving() = true
}