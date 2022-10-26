package com.github.wnebyte.minecraft.renderer;

import java.util.Objects;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.joml.*;
import org.lwjgl.BufferUtils;
import com.github.wnebyte.minecraft.light.PointCaster;
import com.github.wnebyte.minecraft.light.DirectionalCaster;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

public class Shader {

    public static final String U_PROJECTION = "uProjection";

    public static final String U_VIEW = "uView";

    public static final String U_TEXTURE = "uTexture";

    public static final String U_TEX_COORDS_TEXTURE = "uTexCoordsTexture";

    public static final String U_TEXTURES = "uTextures";

    public static final String ACCUM = "accum";

    public static final String REVEAL = "reveal";

    public static final String SCREEN = "screen";

    public static final String U_DAY_CUBEMAP = "uDayCubemap";

    public static final String U_NIGHT_CUBEMAP = "uNightCubemap";

    public static final String U_BLEND = "uBlend";

    public static final String U_INVERSE_PROJECTION = "uInverseProjection";

    public static final String U_INVERSE_VIEW = "uInverseView";

    public static final String U_NORMAL_TEXTURE = "uNormalTexture";

    public static final String U_FONT_TEXTURE = "uFontTexture";

    public static final String U_VIEW_POS = "uViewPos";

    public static final String U_MATERIALS = "uMaterials";

    public static final String U_DIFFUSE_MAPS = "uDiffuseMaps";

    public static final String U_SPECULAR_MAPS = "uSpecularMaps";

    public static final String U_LIGHT = "uLight";

    public static final String U_LIGHT_DIRECTION = "uLight.direction";

    public static final String U_LIGHT_POSITION = "uLight.position";

    public static final String U_LIGHT_AMBIENT = "uLight.ambient";

    public static final String U_LIGHT_DIFFUSE = "uLight.diffuse";

    public static final String U_LIGHT_SPECULAR = "uLight.specular";

    public static final String U_LIGHT_CONSTANT = "uLight.constant";

    public static final String U_LIGHT_LINEAR = "uLight.linear";

    public static final String U_LIGHT_QUADRATIC = "uLight.quadratic";

    public static final String U_POINT_LIGHTS = "uPointLights";

    public static final String U_DIR_LIGHT = "uDirLight";

    private int id;

    private String path;

    private boolean inUse;

    private String vertexSource;

    private String fragmentSource;

    public Shader(String path) {
        this.path = path;
        try {
            String src = new String(Files.readAllBytes(Paths.get(path)));
            String[] split = src.split("(#type)( )+([a-zA-Z]+)");

            // find the first pattern after #type
            int index = src.indexOf("#type") + 6;
            int eol = src.indexOf(System.lineSeparator(), index);
            String firstPattern = src.substring(index, eol).trim();

            // find the second pattern after #type
            index = src.indexOf("#type", eol) + 6;
            eol = src.indexOf(System.lineSeparator(), index);
            String secondPattern = src.substring(index, eol).trim();

            if (firstPattern.equals("vertex")) {
                vertexSource = split[1];
            } else if (firstPattern.equals("fragment")) {
                fragmentSource = split[1];
            } else {
                throw new IOException(
                        "Unexpected token: '" + firstPattern + "'."
                );
            }
            if (secondPattern.equals("vertex")) {
                vertexSource = split[2];
            } else if (secondPattern.equals("fragment")) {
                fragmentSource = split[2];
            } else {
                throw new IOException(
                        "Unexpected token: '" + secondPattern + "'."
                );
            }

        } catch (IOException e) {
            e.printStackTrace();
            assert false : "Error: Could not open file for shader: '" + path + "'.";
        }

    }

    public void compile() {
        int vertexID;
        int fragmentID;

        // ===========================
        // Compile and link shaders
        // ===========================

        // First load and compile the vertex shader
        vertexID = glCreateShader(GL_VERTEX_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(vertexID, vertexSource);
        glCompileShader(vertexID);

        // Check for errors in compilation process
        int success = glGetShaderi(vertexID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(vertexID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + path + "'\n\tVertex shader compilation failed.");
            System.out.println(glGetShaderInfoLog(vertexID, len));
            assert false : "";
        }

        // Second load and compile the fragment shader
        fragmentID = glCreateShader(GL_FRAGMENT_SHADER);
        // Pass the shader source to the GPU
        glShaderSource(fragmentID, fragmentSource);
        glCompileShader(fragmentID);

        // Check for errors in compilation process
        success = glGetShaderi(fragmentID, GL_COMPILE_STATUS);
        if (success == GL_FALSE) {
            int len = glGetShaderi(fragmentID, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + path + "\n\tFragment shader compilation failed.");
            System.out.println(glGetShaderInfoLog(fragmentID, len));
            assert false : "";
        }

        // Todo: move to separate link method
        // Link shaders and check for errors
        id = glCreateProgram();
        glAttachShader(id, vertexID);
        glAttachShader(id, fragmentID);
        glLinkProgram(id);

        // Check for linking errors
        success = glGetProgrami(id, GL_LINK_STATUS);
        if (success == GL_FALSE) {
            int len = glGetProgrami(id, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: '" + path + "'\n\tLinking of shaders failed.");
            System.out.println(glGetProgramInfoLog(fragmentID, len));
            assert false : "";
        }
    }

    public void use() {
        if (!inUse) {
            glUseProgram(id);
            inUse = true;
        }
    }

    public void detach() {
        glUseProgram(0);
        inUse = false;
    }

    public int getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getVertexSource() {
        return vertexSource;
    }

    public String getFragmentSource() {
        return fragmentSource;
    }

    public void uploadMatrix4f(String varName, Matrix4f mat4f) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        FloatBuffer mat4fBuffer = BufferUtils.createFloatBuffer(16);
        mat4f.get(mat4fBuffer);
        glUniformMatrix4fv(varLocation, false, mat4fBuffer);
    }

    public void uploadMatrix3f(String varName, Matrix3f mat3f) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        FloatBuffer mat3fBuffer = BufferUtils.createFloatBuffer(9);
        mat3f.get(mat3fBuffer);
        glUniformMatrix3fv(varLocation, false, mat3fBuffer);
    }

    public void uploadVec4f(String varName, Vector4f vec4f) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform4f(varLocation, vec4f.x, vec4f.y, vec4f.z, vec4f.w);
    }

    public void uploadVec3f(String varName, Vector3f vec3f) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform3f(varLocation, vec3f.x, vec3f.y, vec3f.z);
    }

    public void uploadVec2f(String varName, Vector2f vec2f) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform2f(varLocation, vec2f.x, vec2f.y);
    }

    public void uploadFloat(String varName, float val) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform1f(varLocation, val);
    }

    public void uploadInt(String varName, int val) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform1i(varLocation, val);
    }

    public void uploadTexture(String varName, int slot) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform1i(varLocation, slot);
    }

    public void uploadIntArray(String varName, int[] array) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform1iv(varLocation, array);
    }

    public void uploadFloatArray(String varName, float[] array) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        glUniform1fv(varLocation, array);
    }

    public void uploadPointCasterArray(String varName, PointCaster[] array) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        for (int i = 0; i < array.length; i++) {
            PointCaster caster = array[i];
            uploadVec3f(varName + "[" + i + "].position", caster.getPosition());
            uploadFloat(varName + "[" + i + "].constant", caster.getConstant());
            uploadFloat(varName + "[" + i + "].linear", caster.getLinear());
            uploadFloat(varName + "[" + i + "].quadratic", caster.getQuadratic());
            uploadVec3f(varName + "[" + i + "].ambient", caster.getLight().getAmbient());
            uploadVec3f(varName + "[" + i + "].diffuse", caster.getLight().getDiffuse());
            uploadVec3f(varName + "[" + i + "].specular", caster.getLight().getSpecular());
        }
    }

    public void uploadDirectionalCaster(String varName, DirectionalCaster dirCaster) {
        int varLocation = glGetUniformLocation(id, varName);
        use();
        uploadVec3f(varName + ".direction", dirCaster.getDirection());
        uploadVec3f(varName + ".ambient", dirCaster.getLight().getAmbient());
        uploadVec3f(varName + ".diffuse", dirCaster.getLight().getDiffuse());
        uploadVec3f(varName + ".specular", dirCaster.getLight().getSpecular());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Shader)) return false;
        Shader shader = (Shader) o;
        return Objects.equals(shader.id, this.id) &&
                Objects.equals(shader.path, this.path) &&
                Objects.equals(shader.inUse, this.inUse) &&
                Objects.equals(shader.vertexSource, this.vertexSource) &&
                Objects.equals(shader.fragmentSource, this.fragmentSource);
    }

    @Override
    public int hashCode() {
        int result = 78;
        return result +
                13 +
                Objects.hashCode(this.id) +
                Objects.hashCode(this.path) +
                Objects.hashCode(this.inUse) +
                Objects.hashCode(this.vertexSource) +
                Objects.hashCode(this.fragmentSource);
    }

    @Override
    public String toString() {
        return String.format(
                "Shader[id: %s, path: %s, inUse: %s]", id, path, inUse
        );
    }
}
