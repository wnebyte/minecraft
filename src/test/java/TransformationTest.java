import com.github.wnebyte.minecraft.util.JMath;
import com.github.wnebyte.minecraft.world.Chunk;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.junit.Test;

public class TransformationTest {

    @Test
    public void test00() {
        Vector3f cameraPos = new Vector3f(17.2f, 51.3f, 20.5f);
        Vector2i chunkCoords = new Vector2i(1, 2);
        int index = Chunk.toIndex(2, 50, 14);
        Vector3f index3D = JMath.toVector3f(Chunk.toIndex3D(index));
        System.out.printf("x: %.1f, y: %.1f, z: %.1f%n", index3D.x, index3D.y, index3D.z);
        Vector3f worldPos = new Vector3f(index3D.x + (chunkCoords.x * 16), index3D.y, index3D.z + (chunkCoords.y * 16));
        System.out.printf("x: %.1f, y: %.1f, z: %.1f%n", worldPos.x, worldPos.y, worldPos.z);
    }
}
