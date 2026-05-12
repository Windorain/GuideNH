package com.hfstudio.guidenh.guide.scene;

/**
 * A single billboard particle rendered in the guidebook scene's 3D coordinate space.
 * Simulates the visual appearance of block-breaking or block-placing particles, using a
 * small sub-region of the block's texture atlas icon.
 *
 * <p>
 * Particles are owned by {@link LytGuidebookScene} and advanced one tick per
 * {@link LytGuidebookScene#ponderTick()} call. They are frozen while the ponder animation is
 * paused, and cleared whenever the scene is restarted or an out-of-order seek occurs.
 */
public class GuidebookSceneParticle {

    public float prevX;
    public float prevY;
    public float prevZ;

    /** Current world-space X position. */
    public float x;
    /** Current world-space Y position. */
    public float y;
    /** Current world-space Z position. */
    public float z;

    /** Velocity along the X axis (blocks per tick). */
    public float vx;
    /** Velocity along the Y axis (blocks per tick). */
    public float vy;
    /** Velocity along the Z axis (blocks per tick). */
    public float vz;

    /** Texture atlas minimum U coordinate. */
    public final float u0;
    /** Texture atlas minimum V coordinate. */
    public final float v0;
    /** Texture atlas maximum U coordinate. */
    public final float u1;
    /** Texture atlas maximum V coordinate. */
    public final float v1;

    /** Red color channel multiplier. */
    public final float red;
    /** Green color channel multiplier. */
    public final float green;
    /** Blue color channel multiplier. */
    public final float blue;

    /** Current age in ticks. */
    public int age;
    /** Age at which the particle is considered dead and removed. */
    public final int maxAge;
    /** Half-side length of the billboard quad, in block units. */
    public final float size;

    public GuidebookSceneParticle(float x, float y, float z, float vx, float vy, float vz, float u0, float v0, float u1,
        float v1, float red, float green, float blue, int maxAge, float size) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.prevX = x;
        this.prevY = y;
        this.prevZ = z;
        this.vx = vx;
        this.vy = vy;
        this.vz = vz;
        this.u0 = u0;
        this.v0 = v0;
        this.u1 = u1;
        this.v1 = v1;
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.maxAge = maxAge;
        this.size = size;
        this.age = 0;
    }

    /** Advances the particle by one tick: applies velocity, gravity, and friction. */
    public void tick() {
        prevX = x;
        prevY = y;
        prevZ = z;
        x += vx;
        y += vy;
        z += vz;
        vy -= 0.04f;
        vx *= 0.98f;
        vy *= 0.98f;
        vz *= 0.98f;
        age++;
    }

    /** Returns {@code true} when the particle has exceeded its maximum age. */
    public boolean isDead() {
        return age >= maxAge;
    }

    /** Returns the vanilla digging particle alpha. */
    public float getAlpha() {
        return 1f;
    }

    public float getRenderX(float partialTicks) {
        return interpolate(prevX, x, partialTicks);
    }

    public float getRenderY(float partialTicks) {
        return interpolate(prevY, y, partialTicks);
    }

    public float getRenderZ(float partialTicks) {
        return interpolate(prevZ, z, partialTicks);
    }

    public float getAlpha(float partialTicks) {
        return getAlpha();
    }

    private static float interpolate(float previous, float current, float partialTicks) {
        float t = clampPartialTicks(partialTicks);
        return previous + (current - previous) * t;
    }

    private static float clampPartialTicks(float partialTicks) {
        if (partialTicks <= 0f) {
            return 0f;
        }
        if (partialTicks >= 1f) {
            return 1f;
        }
        return partialTicks;
    }
}
