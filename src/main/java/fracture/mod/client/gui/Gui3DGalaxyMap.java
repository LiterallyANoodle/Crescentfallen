package fracture.mod.client.gui;

import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.galaxies.Moon;
import micdoodle8.mods.galacticraft.api.galaxies.Planet;
import micdoodle8.mods.galacticraft.api.galaxies.SolarSystem;
import micdoodle8.mods.galacticraft.core.client.gui.screen.GuiCelestialSelection;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Project;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Gui3DGalaxyMap extends GuiCelestialSelection {

    // --- Core Cache Storage ---
    private final Map<String, CachedSystem> systemCache = new HashMap<>();
    private CachedSystem activeSystem;
    private boolean isSystemMenuOpen = false;

    // --- Asteroid Populators ---
    private final List<CachedAsteroid> ambientAsteroids = new ArrayList<>();
    private final Map<CachedBody, List<CachedAsteroid>> beltPopulations = new HashMap<>(); 
    private static final ResourceLocation ASTEROID_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/moon.png");
    private static final ResourceLocation SUN_TEXTURE = new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/sun.png");

    // --- Milky Way Generator ---
    private final List<List<GalaxyParticle>> gasLayers = new ArrayList<>();
    private final List<List<GalaxyParticle>> starLayers = new ArrayList<>();
    private float galaxyFadeAlpha = 0.0f;

    // --- Camera & Physics Parameters ---
    private float cameraX = 0.0f;
    private float cameraZ = 0.0f;
    private final float BASE_CAMERA_DISTANCE = 120.0f; 

    // --- Dynamic Camera States ---
    private float currentPitch = 45.0f;
    private float targetPitch = 45.0f;
    private float currentOrbitTilt = 0.0f;
    private float targetOrbitTilt = 0.0f;
    
    private float currentMacroScale = 3.0f;
    private float targetMacroScale = 3.0f;
    
    private boolean showMoonsMacro = true;
    private boolean showZonesMacro = true;
    private float moonFadeAlpha = 0.0f;

    // --- State Machine & Interaction ---
    private ViewLevel currentViewLevel = ViewLevel.SOLAR_SYSTEM;
    private String highlightedStarSystemName = null;

    // --- Belt Interaction Tracking ---
    private float beltClickX = 0.0f;
    private float beltClickZ = 0.0f;

    // --- Inertia & Kinematics ---
    private float velocityX = 0.0f;
    private float velocityZ = 0.0f;
    private final float PAN_SPEED = 0.45f;
    private final float FRICTION_COEFFICIENT = 0.88f;
    private int lastMouseX = -1;
    private int lastMouseY = -1;
    private boolean isRightDragging = false;

    // --- Interaction & Zoom ---
    private float targetCameraX = 0.0f;
    private float targetCameraZ = 0.0f;
    private float currentZoom = 1.0f;
    private float targetZoom = 1.0f;
    private float unrelatedAlpha = 1.0f; 
    private final float ORBIT_SPEED_MULTIPLIER = 0.02f; 

    // --- Raycasting Buffers ---
    private final FloatBuffer savedModelMatrix = BufferUtils.createFloatBuffer(16);
    private final FloatBuffer savedProjMatrix = BufferUtils.createFloatBuffer(16);
    private final IntBuffer savedViewport = BufferUtils.createIntBuffer(16);

    public Gui3DGalaxyMap(boolean isMap, List<CelestialBody> possibleBodies) {
        super(isMap, possibleBodies, false);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.buildCelestialCache();
        this.buildBeltPopulations(); 
        this.spawnAmbientAsteroids(); 
        this.buildMilkyWayCache(); 
    }

    private void buildCelestialCache() {
        systemCache.clear();

        for (SolarSystem solarSystem : GalaxyRegistry.getRegisteredSolarSystems().values()) {
            if (solarSystem == null) continue;

            CachedSystem sysCache = new CachedSystem(solarSystem.getName(), solarSystem);

            if (solarSystem.getMainStar() != null) {
                sysCache.star = new CachedBody(solarSystem.getMainStar(), false, true, null);
            }

            for (Planet planet : GalaxyRegistry.getPlanetsForSolarSystem(solarSystem)) {
                if (planet == null) continue;

                CachedBody cachedPlanet = new CachedBody(planet, false, false, null);
                sysCache.planets.add(cachedPlanet);

                for (Moon moon : GalaxyRegistry.getMoonsForPlanet(planet)) {
                    if (moon == null) continue;
                    cachedPlanet.moons.add(new CachedBody(moon, true, false, cachedPlanet));
                }
            }

            systemCache.put(sysCache.name, sysCache);

            if (sysCache.name.equalsIgnoreCase("Sol") || sysCache.name.equalsIgnoreCase("Solar System")) {
                this.activeSystem = sysCache;
            }
        }

        if (this.activeSystem == null && !systemCache.isEmpty()) {
            this.activeSystem = systemCache.values().iterator().next();
        }
    }

    private void buildBeltPopulations() {
        this.beltPopulations.clear();
        Random rand = new Random();

        for (CachedSystem sys : this.systemCache.values()) {
            for (CachedBody body : sys.planets) {
                if (body.isAsteroidBelt) {
                    List<CachedAsteroid> beltRocks = new ArrayList<>();
                    int count = body.isKuiperBelt ? 400 : 250; 
                    for (int i = 0; i < count; i++) {
                        beltRocks.add(CachedAsteroid.createBeltAsteroid(rand, body.beltWidth));
                    }
                    this.beltPopulations.put(body, beltRocks);
                }
            }
        }
    }

    // PROCEDURAL MILKY WAY GENERATOR (Volumetric Art Pass)

    private void buildMilkyWayCache() {
        this.gasLayers.clear();
        this.starLayers.clear();
        for (int i = 0; i < 3; i++) {
            this.gasLayers.add(new ArrayList<>());
            this.starLayers.add(new ArrayList<>());
        }

        Random rand = new Random(1024); 
        int totalGasParticles = 3500;
        int totalStarParticles = 1800;
        int arms = 4;
        
        float a = 1.5f; 
        float b = 28.0f; 
        float maxTheta = 5.0f * (float)Math.PI;

        for (int i = 0; i < totalGasParticles; i++) {
            int arm = i % arms;
            
            float t = rand.nextFloat();
            t = (float)Math.pow(t, 1.6); 
            float theta = t * maxTheta;
            float armOffset = arm * ((float)Math.PI * 2.0f / arms);

            float r = a + b * theta;
            float noiseFactor = 3.5f + (r * 0.18f);
            float x = (float)(r * Math.cos(theta + armOffset)) + (float)rand.nextGaussian() * noiseFactor;
            float z = (float)(r * Math.sin(theta + armOffset)) + (float)rand.nextGaussian() * noiseFactor;
            float y = (float)rand.nextGaussian() * (8.5f + (r * 0.05f));

            float red, green, blue;
            if (t < 0.15f) { 
                float localT = t / 0.15f;
                red = 1.0f; green = 1.0f - (0.4f * localT); blue = 0.8f - (0.8f * localT);
            } else if (t < 0.5f) { 
                float localT = (t - 0.15f) / 0.35f;
                red = 1.0f - (0.5f * localT); green = 0.6f - (0.4f * localT); blue = 0.0f + (0.8f * localT);
            } else { 
                float localT = (t - 0.5f) / 0.5f;
                red = 0.5f - (0.4f * localT); green = 0.2f - (0.1f * localT); blue = 0.8f + (0.2f * localT);
            }

            float alpha = 0.08f + rand.nextFloat() * 0.15f; 
            float size = 18.0f + rand.nextFloat() * 25.0f; 

            int layer = (t < 0.25f) ? 0 : ((t < 0.65f) ? 1 : 2);
            this.gasLayers.get(layer).add(new GalaxyParticle(x, y, z, red, green, blue, alpha, size));
        }

        for (int i = 0; i < totalStarParticles; i++) {
            int arm = i % arms;
            float t = rand.nextFloat();
            t = (float)Math.pow(t, 1.2); 
            float theta = t * maxTheta;
            float armOffset = arm * ((float)Math.PI * 2.0f / arms);

            float r = a + b * theta;
            float noiseFactor = 1.0f + (r * 0.10f);
            float x = (float)(r * Math.cos(theta + armOffset)) + (float)rand.nextGaussian() * noiseFactor;
            float z = (float)(r * Math.sin(theta + armOffset)) + (float)rand.nextGaussian() * noiseFactor;
            float y = (float)rand.nextGaussian() * (2.0f + (r * 0.02f));

            float red = 0.8f + rand.nextFloat() * 0.2f;
            float green = 0.8f + rand.nextFloat() * 0.2f;
            float blue = (t > 0.4f) ? (0.8f + rand.nextFloat() * 0.2f) : (0.5f + rand.nextFloat() * 0.4f);

            float alpha = 0.6f + rand.nextFloat() * 0.4f;
            float size = 0.5f + rand.nextFloat() * 1.8f;

            int layer = (t < 0.25f) ? 0 : ((t < 0.65f) ? 1 : 2);
            this.starLayers.get(layer).add(new GalaxyParticle(x, y, z, red, green, blue, alpha, size));
        }
    }

    private void renderMilkyWay(float partialTicks, float masterAlphaFade) {
        float globalTime = this.mc.world.getTotalWorldTime() + partialTicks;

        GlStateManager.pushMatrix();
        GlStateManager.scale(15.0f, 15.0f, 15.0f);

        this.mc.getTextureManager().bindTexture(SUN_TEXTURE);
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha(); 
        GlStateManager.depthMask(false); 
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        this.drawFacingTexturedQuad(buffer, 0, 0, 0, 150f, 1.0f, 0.7f, 0.4f, 0.15f * masterAlphaFade);
        this.drawFacingTexturedQuad(buffer, 0, 0, 0, 80f, 1.0f, 0.9f, 0.6f, 0.25f * masterAlphaFade);
        this.drawFacingTexturedQuad(buffer, 0, 0, 0, 35f, 1.0f, 1.0f, 1.0f, 0.45f * masterAlphaFade);
        tessellator.draw();
        GlStateManager.popMatrix();

        float[] layerRotations = { 0.06f, 0.035f, 0.012f };

        // Gas Clouds
        for (int i = 0; i < this.gasLayers.size(); i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(globalTime * layerRotations[i], 0, 1, 0);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (GalaxyParticle p : this.gasLayers.get(i)) {
                this.drawFacingTexturedQuad(buffer, p.x, p.y, p.z, p.size, p.r, p.g, p.b, p.alpha * masterAlphaFade);
            }
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        // Solid Stars
        for (int i = 0; i < this.starLayers.size(); i++) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(globalTime * layerRotations[i], 0, 1, 0);
            
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
            for (GalaxyParticle p : this.starLayers.get(i)) {
                this.drawFacingTexturedQuad(buffer, p.x, p.y, p.z, p.size, p.r, p.g, p.b, p.alpha * masterAlphaFade);
            }
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        // 3D Star System Nodes
        GlStateManager.depthMask(true); 
        GlStateManager.enableAlpha();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        GlStateManager.pushMatrix();
        GlStateManager.rotate(globalTime * 0.035f, 0, 1, 0); 

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, savedModelMatrix);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, savedProjMatrix);
        GL11.glGetInteger(GL11.GL_VIEWPORT, savedViewport);
        ScaledResolution sr = new ScaledResolution(this.mc);
        int scaleFactor = sr.getScaleFactor();

        for (CachedSystem sys : this.systemCache.values()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(sys.galaxyX, 0, sys.galaxyZ);
            GlStateManager.rotate(globalTime * 5.0f, 0.0f, 1.0f, 0.0f); // Spin in place
            
            if (sys.star != null) {
                this.mc.getTextureManager().bindTexture(sys.star.texture);
            } else {
                this.mc.getTextureManager().bindTexture(SUN_TEXTURE);
            }
            
            GlStateManager.color(1.0f, 1.0f, 1.0f, masterAlphaFade);
            this.drawTexturedCube(2.5f); 
            GlStateManager.popMatrix();

            // Project 3D vector to 2D screen coordinate
            FloatBuffer winCoords = BufferUtils.createFloatBuffer(3);
            if (Project.gluProject(sys.galaxyX, 0, sys.galaxyZ, savedModelMatrix, savedProjMatrix, savedViewport, winCoords)) {
                sys.projectedX = winCoords.get(0) / scaleFactor;
                sys.projectedY = (this.mc.displayHeight - winCoords.get(1)) / scaleFactor;
                
                sys.isVisible = winCoords.get(2) > 0.0f && winCoords.get(2) < 1.0f && 
                                sys.projectedX >= 0 && sys.projectedX <= this.width &&
                                sys.projectedY >= 0 && sys.projectedY <= this.height;
            } else {
                sys.isVisible = false;
            }
        }
        
        GlStateManager.popMatrix(); 
        GlStateManager.popMatrix(); 
    }

    private void drawFacingTexturedQuad(BufferBuilder buffer, float x, float y, float z, float size, float r, float g, float b, float a) {
        float half = size / 2.0f;
        buffer.pos(x - half, y, z - half).tex(0, 0).color(r, g, b, a).endVertex();
        buffer.pos(x - half, y, z + half).tex(0, 1).color(r, g, b, a).endVertex();
        buffer.pos(x + half, y, z + half).tex(1, 1).color(r, g, b, a).endVertex();
        buffer.pos(x + half, y, z - half).tex(1, 0).color(r, g, b, a).endVertex();
    }

    private void switchSystem(CachedSystem sys) {
        this.activeSystem = sys;
        this.unselectCelestialBody();
        this.selectedParent = sys.nativeSystemReference;
        
        // Critical Fix: Hard reset of target tracking values to prevent deep space drift
        this.targetCameraX = 0.0f;
        this.targetCameraZ = 0.0f;
        this.cameraX = 0.0f;
        this.cameraZ = 0.0f;
        this.targetZoom = 1.2f;
        
        try {
            super.initGui();
            this.spawnAmbientAsteroids(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Ambient asteroids
    
    private void spawnAmbientAsteroids() {
        this.ambientAsteroids.clear();
        Random rand = new Random();
        
        for (int i = 0; i < 150; i++) {
            CachedAsteroid ast = new CachedAsteroid(rand);
            ast.respawn(rand, null); 
            ast.age = rand.nextInt(ast.maxAge); 
            ast.fadeAlpha = (rand.nextFloat() * 0.5f) + 0.5f; 
            this.ambientAsteroids.add(ast);
        }
    }

    private void updateAndRenderAsteroids(float partialTicks) {
        if (this.ambientAsteroids.isEmpty() || this.unrelatedAlpha < 0.1f) return;

        GlStateManager.pushMatrix();
        
        try {
            this.mc.getTextureManager().bindTexture(ASTEROID_TEXTURE);
        } catch (Exception e) {
            GlStateManager.disableTexture2D(); 
        }

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        float zoomFade = Math.max(0.0f, Math.min(1.0f, (this.currentZoom - 0.2f) / 0.6f));
        float baseAlpha = (this.currentViewLevel == ViewLevel.LOCAL_PLANET ? this.unrelatedAlpha * 0.3f : this.unrelatedAlpha) * zoomFade;

        if (baseAlpha <= 0.01f) {
            GlStateManager.disableBlend();
            GlStateManager.enableTexture2D();
            GlStateManager.popMatrix();
            return;
        }

        Random rand = new Random();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        for (CachedAsteroid ast : this.ambientAsteroids) {
            
            ast.age++;
            if (ast.age >= ast.maxAge) {
                ast.fadeAlpha -= 0.02f; 
                if (ast.fadeAlpha <= 0.0f) {
                    CachedAsteroid clusterTarget = (rand.nextFloat() < 0.2f) ? this.ambientAsteroids.get(rand.nextInt(this.ambientAsteroids.size())) : null;
                    ast.respawn(rand, clusterTarget);
                }
            } else if (ast.fadeAlpha < 1.0f) {
                ast.fadeAlpha += 0.02f; 
                if (ast.fadeAlpha > 1.0f) ast.fadeAlpha = 1.0f;
            }

            ast.x += ast.vx;
            ast.y += ast.vy;
            ast.z += ast.vz;
            ast.pitch += ast.rotSpeedPitch;
            ast.yaw += ast.rotSpeedYaw;
            ast.roll += ast.rotSpeedRoll;

            float renderAlpha = baseAlpha * ast.fadeAlpha;
            if (renderAlpha <= 0.01f) continue; 

            GlStateManager.color(0.8f, 0.8f, 0.8f, renderAlpha);

            GlStateManager.pushMatrix();
            GlStateManager.translate(ast.x, ast.y, ast.z);
            GlStateManager.rotate(ast.yaw, 0, 1, 0);
            GlStateManager.rotate(ast.pitch, 1, 0, 0);
            GlStateManager.rotate(ast.roll, 0, 0, 1);
            GlStateManager.scale(ast.scale, ast.scale, ast.scale);

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
            
            for (int i = 0; i < ast.vertices.length; i++) {
                buffer.pos(ast.vertices[i][0], ast.vertices[i][1], ast.vertices[i][2])
                      .tex(ast.vertices[i][3], ast.vertices[i][4])
                      .endVertex();
            }
            
            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    // Rendering pipeline and camera tracking

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.ticksSinceMenuOpenF += partialTicks;
        this.ticksTotalF += partialTicks;
        if (this.selectedBody != null) {
            this.ticksSinceSelectionF += partialTicks;
        }
        if (this.selectedBody == null && this.ticksSinceUnselectionF >= 0) {
            this.ticksSinceUnselectionF += partialTicks;
        }

        if (this.selectedBody != null) {
            if (this.currentViewLevel == ViewLevel.LOCAL_PLANET) {
                this.targetMacroScale = 1.0f;
            } else {
                this.targetMacroScale = 3.0f;
            }

            CachedBody activeTarget = getCachedBodyForNative(this.selectedBody);
            if (activeTarget != null) {
                if (activeTarget.isStar) {
                    this.targetCameraX = 0.0f;
                    this.targetCameraZ = 0.0f;
                } else if (activeTarget.isAsteroidBelt) {
                    this.targetCameraX = this.beltClickX;
                    this.targetCameraZ = this.beltClickZ;
                } else {
                    float globalTime = this.mc.world.getTotalWorldTime() + partialTicks;
                    float theta = ((globalTime / activeTarget.orbitTime) * ORBIT_SPEED_MULTIPLIER) + activeTarget.phaseShift;
                    float scaledDist = activeTarget.orbitDistance * (activeTarget.isMoon ? 1.5f : 30.0f * this.currentMacroScale);
                    
                    float parentX = 0.0f;
                    float parentZ = 0.0f;
                    
                    if (activeTarget.isMoon && activeTarget.parentPlanet != null) {
                        float pTheta = ((globalTime / activeTarget.parentPlanet.orbitTime) * ORBIT_SPEED_MULTIPLIER) + activeTarget.parentPlanet.phaseShift;
                        float pDist = activeTarget.parentPlanet.orbitDistance * 30.0f * this.currentMacroScale;
                        parentX = (float) Math.cos(pTheta) * pDist;
                        parentZ = (float) Math.sin(pTheta) * pDist;
                        
                        double localX = Math.cos(theta) * scaledDist;
                        double localY = 0;
                        double localZ = Math.sin(theta) * scaledDist;

                        double zRad = Math.toRadians(this.currentOrbitTilt * 0.7f);
                        double x1 = localX * Math.cos(zRad) - localY * Math.sin(zRad);
                        double y1 = localX * Math.sin(zRad) + localY * Math.cos(zRad);
                        double z1 = localZ;

                        double xRad = Math.toRadians(-this.currentOrbitTilt);
                        double x2 = x1;
                        double y2 = y1 * Math.cos(xRad) - z1 * Math.sin(xRad);
                        double z2 = y1 * Math.sin(xRad) + z1 * Math.cos(xRad);
                        
                        this.targetCameraX = parentX + (float) x2;
                        this.targetCameraZ = parentZ + (float) z2;
                    } else {
                        this.targetCameraX = parentX + (float) Math.cos(theta) * scaledDist;
                        this.targetCameraZ = parentZ + (float) Math.sin(theta) * scaledDist;
                    }
                }
            }
        } else {
            this.updateCameraKinematics(mouseX, mouseY);
            
            if (this.currentViewLevel == ViewLevel.GALAXY) {
                this.targetPitch = 75.0f; 
                this.targetOrbitTilt = 0.0f;
            } else {
                this.targetMacroScale = 3.0f;
            }
        }

        this.interpolateCamera();

        float targetFade = (this.selectionState == GuiCelestialSelection.EnumSelection.ZOOMED && this.selectedBody != null) ? 0.0f : 1.0f;
        this.unrelatedAlpha += (targetFade - this.unrelatedAlpha) * 0.15f;
        
        float targetMoonFade = (!this.showMoonsMacro && this.selectedBody == null) ? 0.0f : 1.0f;
        this.moonFadeAlpha += (targetMoonFade - this.moonFadeAlpha) * 0.15f;

        float targetGalaxyAlpha = (this.currentViewLevel == ViewLevel.GALAXY) ? 1.0f : 0.0f;
        this.galaxyFadeAlpha += (targetGalaxyAlpha - this.galaxyFadeAlpha) * 0.08f;

        GlStateManager.clearColor(0.05f, 0.05f, 0.08f, 1.0f); 
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        this.setup3DViewport();
        
        if (this.galaxyFadeAlpha > 0.01f) {
            this.renderMilkyWay(partialTicks, this.galaxyFadeAlpha);
        }

        if (this.currentViewLevel != ViewLevel.GALAXY) {
            this.renderSystem(this.activeSystem, partialTicks);
        }
        
        this.updateAndRenderAsteroids(partialTicks);
        
        this.restore2DViewport();

        this.planetPosMap.clear(); 
        
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        
        try {
            if (this.currentViewLevel != ViewLevel.GALAXY) {
                this.drawButtons(mouseX, mouseY); 
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        this.drawBorder(); 
        this.drawCustomUI(mouseX, mouseY); 
        
        GlStateManager.popMatrix();
    }

    @Override
    public void drawDefaultBackground() {
    }

    // Custom UI
    
    private void drawCustomUI(int mouseX, int mouseY) {
        int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
        int TOP = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
        int RHS = this.width - LHS;
        int BOTTOM = this.height - LHS;

        if (this.currentViewLevel != ViewLevel.GALAXY) {
            if (this.isSystemMenuOpen) {
                int menuX = LHS + 105;
                int listY = TOP + 14;
                
                for (CachedSystem sys : this.systemCache.values()) {
                    boolean hovered = mouseX >= menuX && mouseX <= menuX + 93 && mouseY >= listY && mouseY <= listY + 17;
                    this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
                    
                    if (hovered) {
                        GL11.glColor4f(0.0F, 1.0F, 0.0F, 1); 
                    } else {
                        GL11.glColor4f(0.0F, 0.6F, 1.0F, 1); 
                    }
                    
                    this.drawTexturedModalRect(menuX, listY, 93, 17, 95, 436, 93, 17, false, false);
                    this.fontRenderer.drawString(sys.name, menuX + 25, listY + 5, 0xFFFFFF);

                    if (sys.star != null) {
                        this.mc.getTextureManager().bindTexture(sys.star.texture);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        this.drawTexturedModalRect(menuX + 4, listY + 1, 14, 14, 0, 0, 16, 16, false, false, 16, 16);
                    }
                    listY += 18;
                }
            }
        } else {
            this.highlightedStarSystemName = null;
            
            for (CachedSystem sys : this.systemCache.values()) {
                sys.isHovered = false;
                
                if (sys.isVisible) {
                    float startX = sys.projectedX;
                    float startY = sys.projectedY;
                    float elbowX = startX + 25.0f;
                    float elbowY = startY - 25.0f;
                    
                    sys.textX = elbowX + 2;
                    sys.textY = elbowY - 10;
                    sys.textW = this.fontRenderer.getStringWidth(sys.name);
                    sys.textH = 10;

                    float dist = (float) Math.sqrt(Math.pow(mouseX - sys.projectedX, 2) + Math.pow(mouseY - sys.projectedY, 2));
                    boolean textHovered = mouseX >= sys.textX && mouseX <= sys.textX + sys.textW && mouseY >= sys.textY && mouseY <= sys.textY + sys.textH;

                    if (dist < 15.0f || textHovered) {
                        sys.isHovered = true;
                        this.highlightedStarSystemName = sys.name;
                    }
                    
                    GlStateManager.pushMatrix();
                    GlStateManager.disableTexture2D();
                    GlStateManager.enableBlend();
                    GL11.glEnable(GL11.GL_LINE_SMOOTH);
                    GL11.glLineWidth(2.0f);

                    if (sys.isHovered) {
                        GlStateManager.color(1.0f, 1.0f, 0.0f, 1.0f);
                    } else {
                        GlStateManager.color(0.0f, 0.6f, 1.0f, 0.7f);
                    }

                    Tessellator tess = Tessellator.getInstance();
                    BufferBuilder buf = tess.getBuffer();
                    buf.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);
                    
                    buf.pos(startX, startY, 0).endVertex();
                    buf.pos(elbowX, elbowY, 0).endVertex();
                    buf.pos(elbowX + sys.textW + 4.0f, elbowY, 0).endVertex(); 
                    
                    tess.draw();
                    
                    GlStateManager.enableTexture2D();
                    GlStateManager.disableBlend();
                    GL11.glDisable(GL11.GL_LINE_SMOOTH);
                    GlStateManager.popMatrix();

                    int textColor = sys.isHovered ? 0xFFFF00 : 0x00A0FF;
                    this.fontRenderer.drawStringWithShadow(sys.name, sys.textX, sys.textY, textColor);
                }
            }

            this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
            GL11.glColor4f(0.45F, 0.05F, 0.75F, 1.0F); 
            this.drawTexturedModalRect(LHS, TOP, 93, 17, 95, 436, 93, 17, false, false);
            
            int textWidth = this.fontRenderer.getStringWidth("Milky Way");
            this.fontRenderer.drawString("Milky Way", LHS + (93 / 2) - (textWidth / 2), TOP + 5, 0xDDA0DD);
        }

        if (this.selectedBody != null) {
            if (this.selectedBody.getReachable()) {
                this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
                GL11.glColor4f(0.0F, 1.0F, 0.0F, 1);
                this.drawTexturedModalRect(RHS - 74, TOP, 74, 11, 0, 392, 148, 22, true, false);
                String strLaunch = GCCoreUtil.translate("gui.message.launch.name").toUpperCase();
                this.fontRenderer.drawString(strLaunch, RHS - 40 - this.fontRenderer.getStringWidth(strLaunch) / 2, TOP + 2, 0xFFFFFF);
            }

            if (this.mc.player.capabilities.isCreativeMode) {
                this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
                GL11.glColor4f(1.0F, 0.0F, 1.0F, 1);
                this.drawTexturedModalRect(RHS - 74, TOP + 15, 74, 11, 0, 392, 148, 22, true, false);
                String strTeleport = "TELEPORT";
                this.fontRenderer.drawString(strTeleport, RHS - 40 - this.fontRenderer.getStringWidth(strTeleport) / 2, TOP + 17, 0xFFFFFF);
            }
        }

        if (this.selectedBody == null && this.currentViewLevel != ViewLevel.LOCAL_PLANET && this.currentViewLevel != ViewLevel.GALAXY) {
            int btnWidth = 74;
            int btnHeight = 11;
            int btnX = RHS - btnWidth;
            int btnYMoons = BOTTOM - btnHeight; 
            int btnYZones = btnYMoons - 15;

            this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
            boolean hoveredZ = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnYZones && mouseY <= btnYZones + btnHeight;
            if (hoveredZ) GL11.glColor4f(this.showZonesMacro ? 0.0F : 1.0F, this.showZonesMacro ? 1.0F : 0.0F, 0.0F, 1);
            else GL11.glColor4f(this.showZonesMacro ? 0.0F : 0.7F, this.showZonesMacro ? 0.7F : 0.0F, 0.0F, 1);
            
            this.drawTexturedModalRect(btnX, btnYZones, btnWidth, btnHeight, 0, 392, 148, 22, true, false);
            String strToggleZ = "ZONES: " + (this.showZonesMacro ? "ON" : "OFF");
            this.fontRenderer.drawString(strToggleZ, btnX + btnWidth/2 - this.fontRenderer.getStringWidth(strToggleZ) / 2, btnYZones + 2, 0xFFFFFF);

            this.mc.getTextureManager().bindTexture(GuiCelestialSelection.guiMain0);
            boolean hoveredM = mouseX >= btnX && mouseX <= btnX + btnWidth && mouseY >= btnYMoons && mouseY <= btnYMoons + btnHeight;
            if (hoveredM) GL11.glColor4f(this.showMoonsMacro ? 0.0F : 1.0F, this.showMoonsMacro ? 1.0F : 0.0F, 0.0F, 1);
            else GL11.glColor4f(this.showMoonsMacro ? 0.0F : 0.7F, this.showMoonsMacro ? 0.7F : 0.0F, 0.0F, 1);
            
            this.drawTexturedModalRect(btnX, btnYMoons, btnWidth, btnHeight, 0, 392, 148, 22, true, false);
            String strToggleM = "MOONS: " + (this.showMoonsMacro ? "ON" : "OFF");
            this.fontRenderer.drawString(strToggleM, btnX + btnWidth/2 - this.fontRenderer.getStringWidth(strToggleM) / 2, btnYMoons + 2, 0xFFFFFF);
        }
    }

    private void updateCameraKinematics(int mouseX, int mouseY) {
        float currentPanSpeed = (this.currentViewLevel == ViewLevel.GALAXY) ? PAN_SPEED * 8.0f : PAN_SPEED;

        if (this.selectedBody == null) {
            if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindForward.getKeyCode())) velocityZ -= currentPanSpeed;
            if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindBack.getKeyCode())) velocityZ += currentPanSpeed;
            if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindLeft.getKeyCode())) velocityX -= currentPanSpeed;
            if (Keyboard.isKeyDown(this.mc.gameSettings.keyBindRight.getKeyCode())) velocityX += currentPanSpeed;
        }

        if (Mouse.isButtonDown(1)) {
            if (!isRightDragging) {
                isRightDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                velocityX = 0;
                velocityZ = 0;
            } else {
                int deltaX = mouseX - lastMouseX;
                int deltaY = mouseY - lastMouseY;

                velocityX = -deltaX * currentPanSpeed;
                velocityZ = -deltaY * currentPanSpeed;

                lastMouseX = mouseX;
                lastMouseY = mouseY;
            }
        } else {
            isRightDragging = false;
        }

        cameraX += velocityX;
        cameraZ += velocityZ;
        velocityX *= FRICTION_COEFFICIENT;
        velocityZ *= FRICTION_COEFFICIENT;

        if (Math.abs(velocityX) < 0.01f) velocityX = 0;
        if (Math.abs(velocityZ) < 0.01f) velocityZ = 0;
        
        // Let the target continuously update to the kinematics so we can freely pan in all modes
        if (this.selectedBody == null) {
            this.targetCameraX = this.cameraX;
            this.targetCameraZ = this.cameraZ;
        }
    }

    private void interpolateCamera() {
        float trackingSpeed = (this.selectedBody != null || this.currentViewLevel == ViewLevel.GALAXY) ? 0.40f : 0.12f;
        this.cameraX += (targetCameraX - this.cameraX) * trackingSpeed;
        this.cameraZ += (targetCameraZ - this.cameraZ) * trackingSpeed;
        this.currentZoom += (targetZoom - this.currentZoom) * 0.12f;
        
        this.currentMacroScale += (targetMacroScale - this.currentMacroScale) * 0.12f;
        this.currentPitch += (targetPitch - this.currentPitch) * 0.10f;
        this.currentOrbitTilt += (targetOrbitTilt - this.currentOrbitTilt) * 0.10f;
    }

    // Player input and raycasting

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scrollDelta = Mouse.getEventDWheel();
        if (scrollDelta != 0) {
            if (scrollDelta > 0) {
                this.targetZoom *= 1.2f;
                // Transition triggers only when completely zoomed out to classic limits
                if (this.currentViewLevel == ViewLevel.GALAXY && this.targetZoom >= 0.05f) {
                    this.currentViewLevel = ViewLevel.SOLAR_SYSTEM;
                    
                    // Reset to standard solar system perspective
                    this.targetPitch = 45.0f;
                    this.targetOrbitTilt = 0.0f;
                    this.targetCameraX = 0.0f;
                    this.targetCameraZ = 0.0f;
                    this.cameraX = 0.0f;
                    this.cameraZ = 0.0f;
                }
            } else {
                this.targetZoom /= 1.2f;
                if (this.currentViewLevel == ViewLevel.SOLAR_SYSTEM && this.targetZoom < 0.05f) {
                    this.currentViewLevel = ViewLevel.GALAXY;
                    this.unselectCelestialBody();
                    
                    if (this.activeSystem != null) {
                        this.targetCameraX = this.activeSystem.galaxyX;
                        this.targetCameraZ = this.activeSystem.galaxyZ;
                        this.cameraX = this.activeSystem.galaxyX; 
                        this.cameraZ = this.activeSystem.galaxyZ;
                    }
                }
            }
            
            if (this.targetZoom < 0.002f) this.targetZoom = 0.002f;
            if (this.targetZoom > 20.0f) this.targetZoom = 20.0f;
            
            if (this.selectionState == GuiCelestialSelection.EnumSelection.ZOOMED) {
                this.selectionState = GuiCelestialSelection.EnumSelection.SELECTED;
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0 && this.handleCustomUIClick(mouseX, mouseY)) {
        	//Note; change to custom sound later
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return; 
        }

        // Single-Click Detection for System Nodes from Galaxy Level
        if (this.currentViewLevel == ViewLevel.GALAXY && mouseButton == 0) {
            for (CachedSystem sys : this.systemCache.values()) {
                if (sys.isVisible && sys.isHovered) {
                    this.currentViewLevel = ViewLevel.SOLAR_SYSTEM;
                    this.switchSystem(sys);
                    
                    this.targetPitch = 45.0f; 
                    this.targetOrbitTilt = 0.0f;
                    
                    this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    return;
                }
            }
        }

        int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
        int RHS = this.width - LHS;
        int TOP = LHS;
        
        boolean isInsideLeftPanel = (mouseX >= LHS && mouseX <= LHS + 100 && mouseY >= TOP);
        boolean isInsideRightPanel = (mouseX >= RHS - 100 && mouseX <= RHS && mouseY >= TOP);
        boolean isInsideSystemMenu = isSystemMenuOpen && (mouseX >= LHS + 105 && mouseX <= LHS + 200 && mouseY >= TOP + 14);

        if (!isInsideLeftPanel && !isInsideRightPanel && !isInsideSystemMenu && mouseButton == 0) {
            CelestialBody prevSelected = this.selectedBody;
            CachedBody hitBody = this.raycastToCelestialBody();
            
            if (hitBody != null) {
                if (hitBody.nativeBodyReference == prevSelected) {
                    if (!hitBody.isStar && !hitBody.isMoon && !hitBody.isAsteroidBelt) {
                        this.currentViewLevel = ViewLevel.LOCAL_PLANET;
                        this.targetPitch = 5.0f; 
                        this.targetOrbitTilt = 15.0f; 
                        
                        float maxMoonDist = 1.0f;
                        if (!hitBody.moons.isEmpty()) {
                            for (CachedBody m : hitBody.moons) maxMoonDist = Math.max(maxMoonDist, m.orbitDistance);
                        }
                        this.targetZoom = Math.max(2.0f, 15.0f / (maxMoonDist * 1.5f));
                    }
                    this.selectionState = GuiCelestialSelection.EnumSelection.ZOOMED;
                } else {
                    super.selectedBody = hitBody.nativeBodyReference;
                    this.selectionState = GuiCelestialSelection.EnumSelection.SELECTED;
                    
                    if (this.currentViewLevel == ViewLevel.LOCAL_PLANET && hitBody.isMoon && hitBody.parentPlanet != null && hitBody.parentPlanet.nativeBodyReference == prevSelected) {
                        this.targetZoom = 5.0f; 
                    } else {
                        this.currentViewLevel = ViewLevel.SOLAR_SYSTEM;
                        this.targetPitch = 45.0f;
                        this.targetOrbitTilt = 0.0f;
                        
                        if (hitBody.isAsteroidBelt) {
                            this.targetZoom = 3.5f; 
                        } else {
                            this.targetZoom = hitBody.isStar ? 1.5f : 1.2f; 
                        }
                    }
                }
                this.ticksSinceSelectionF = 0;
            } else {
                if (this.selectedBody != null || this.currentViewLevel == ViewLevel.LOCAL_PLANET) {
                    this.targetPitch = 45.0f;
                    this.targetOrbitTilt = 0.0f;
                }
                this.unselectCelestialBody();
            }
            return; 
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean handleCustomUIClick(int mouseX, int mouseY) {
        int LHS = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
        int TOP = GuiCelestialSelection.BORDER_SIZE + GuiCelestialSelection.BORDER_EDGE_SIZE;
        int RHS = this.width - LHS;
        int BOTTOM = this.height - LHS;

        if (this.currentViewLevel != ViewLevel.GALAXY) {
            if (mouseX >= LHS + 2 && mouseX <= LHS + 95 && mouseY >= TOP + 14 && mouseY <= TOP + 31) {
                this.isSystemMenuOpen = !this.isSystemMenuOpen;
                return true;
            }

            if (this.isSystemMenuOpen) {
                int menuX = LHS + 105;
                int listY = TOP + 14;
                for (CachedSystem sys : this.systemCache.values()) {
                    if (mouseX >= menuX && mouseX <= menuX + 93 && mouseY >= listY && mouseY <= listY + 17) {
                        this.switchSystem(sys);
                        this.isSystemMenuOpen = false;
                        return true;
                    }
                    listY += 18;
                }
            }
        }

        if (this.selectedBody != null) {
            if (this.selectedBody.getReachable()) {
                if (mouseX >= RHS - 74 && mouseX <= RHS && mouseY >= TOP && mouseY <= TOP + 11) {
                    this.teleportToSelectedBody(); 
                    return true;
                }
            }
            if (this.mc.player.capabilities.isCreativeMode) {
                if (mouseX >= RHS - 74 && mouseX <= RHS && mouseY >= TOP + 15 && mouseY <= TOP + 26) {
                    int dimID = this.selectedBody.getDimensionID();
                    micdoodle8.mods.galacticraft.core.GalacticraftCore.packetPipeline.sendToServer(
                        new micdoodle8.mods.galacticraft.core.network.PacketSimple(
                            micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket.S_TELEPORT_ENTITY, 
                            micdoodle8.mods.galacticraft.core.util.GCCoreUtil.getDimensionID(this.mc.world), 
                            new Object[] { dimID }
                        )
                    );
                    this.mc.displayGuiScreen(new micdoodle8.mods.galacticraft.core.client.gui.screen.GuiTeleporting(dimID));
                    return true;
                }
            }
        }
        
        if (this.selectedBody == null && this.currentViewLevel != ViewLevel.LOCAL_PLANET && this.currentViewLevel != ViewLevel.GALAXY) {
            int btnX = RHS - 74;
            int btnYMoons = BOTTOM - 11;
            int btnYZones = btnYMoons - 15;
            
            if (mouseX >= btnX && mouseX <= btnX + 74 && mouseY >= btnYZones && mouseY <= btnYZones + 11) {
                this.showZonesMacro = !this.showZonesMacro;
                return true; 
            }
            
            if (mouseX >= btnX && mouseX <= btnX + 74 && mouseY >= btnYMoons && mouseY <= btnYMoons + 11) {
                this.showMoonsMacro = !this.showMoonsMacro;
                return true; 
            }
        }
        return false;
    }
    
    private CachedBody raycastToCelestialBody() {
        if (this.currentViewLevel == ViewLevel.GALAXY) return null;

        float winX = (float) Mouse.getX();
        float winY = (float) Mouse.getY(); 

        FloatBuffer nearPos = BufferUtils.createFloatBuffer(3);
        Project.gluUnProject(winX, winY, 0.0f, savedModelMatrix, savedProjMatrix, savedViewport, nearPos);
        Vec3d rayOrigin = new Vec3d(nearPos.get(0), nearPos.get(1), nearPos.get(2));

        FloatBuffer farPos = BufferUtils.createFloatBuffer(3);
        Project.gluUnProject(winX, winY, 1.0f, savedModelMatrix, savedProjMatrix, savedViewport, farPos);
        Vec3d rayTarget = new Vec3d(farPos.get(0), farPos.get(1), farPos.get(2));

        Vec3d rayDir = rayTarget.subtract(rayOrigin).normalize();
        float globalTime = this.mc.world.getTotalWorldTime() + this.mc.getRenderPartialTicks();
        
        if (this.activeSystem.star != null && this.isBodyRelated(this.activeSystem.star) && this.unrelatedAlpha > 0.1f) {
            if (isRayIntersectingSphere(rayOrigin, rayDir, new Vec3d(0,0,0), 8.0f)) return this.activeSystem.star;
        }

        CelestialBody activePlanetRef = this.selectedBody;
        if (this.selectedBody instanceof Moon) {
            activePlanetRef = ((Moon) this.selectedBody).getParentPlanet();
        }

        for (CachedBody body : this.activeSystem.planets) {
            
            if (this.currentViewLevel == ViewLevel.LOCAL_PLANET && this.selectedBody != null && body.nativeBodyReference != activePlanetRef) {
                continue; 
            }
            
            float theta = ((globalTime / body.orbitTime) * ORBIT_SPEED_MULTIPLIER) + body.phaseShift;
            float scaledDist = body.orbitDistance * 30.0f * this.currentMacroScale;
            Vec3d bodyCenter = new Vec3d(Math.cos(theta) * scaledDist, 0.0, Math.sin(theta) * scaledDist);

            if ((this.isBodyRelated(body) || this.unrelatedAlpha > 0.1f) && isRayIntersectingSphere(rayOrigin, rayDir, bodyCenter, 4.0f)) return body; 
            
            for (CachedBody moon : body.moons) {
                float mTheta = ((globalTime / moon.orbitTime) * ORBIT_SPEED_MULTIPLIER) + moon.phaseShift;
                float mScaledDist = moon.orbitDistance * 1.5f; 
                
                double localX = Math.cos(mTheta) * mScaledDist;
                double localY = 0;
                double localZ = Math.sin(mTheta) * mScaledDist;

                double zRad = Math.toRadians(this.currentOrbitTilt * 0.7f);
                double x1 = localX * Math.cos(zRad) - localY * Math.sin(zRad);
                double y1 = localX * Math.sin(zRad) + localY * Math.cos(zRad);
                double z1 = localZ;

                double xRad = Math.toRadians(-this.currentOrbitTilt);
                double x2 = x1;
                double y2 = y1 * Math.cos(xRad) - z1 * Math.sin(xRad);
                double z2 = y1 * Math.sin(xRad) + z1 * Math.cos(xRad);

                Vec3d moonCenter = new Vec3d(bodyCenter.x + x2, bodyCenter.y + y2, bodyCenter.z + z2);
                
                if ((this.isBodyRelated(moon) || this.unrelatedAlpha > 0.1f) && isRayIntersectingSphere(rayOrigin, rayDir, moonCenter, 2.5f)) return moon;
            }
        }

        if (rayDir.y != 0.0) {
            double t = -rayOrigin.y / rayDir.y; 
            if (t > 0) {
                double intersectX = rayOrigin.x + t * rayDir.x;
                double intersectZ = rayOrigin.z + t * rayDir.z;
                double distFromCenter = Math.sqrt(intersectX * intersectX + intersectZ * intersectZ);

                for (CachedBody body : this.activeSystem.planets) {
                    if (this.currentViewLevel == ViewLevel.LOCAL_PLANET && this.selectedBody != null && body.nativeBodyReference != activePlanetRef) {
                        continue; 
                    }
                    if (body.isAsteroidBelt) {
                        float scaledDist = body.orbitDistance * 30.0f * this.currentMacroScale;
                        float halfWidth = (body.beltWidth * this.currentMacroScale) / 2.0f; 
                        
                        if (distFromCenter >= scaledDist - halfWidth && distFromCenter <= scaledDist + halfWidth) {
                            if (this.isBodyRelated(body) || this.unrelatedAlpha > 0.1f) {
                                this.beltClickX = (float) intersectX;
                                this.beltClickZ = (float) intersectZ;
                                return body;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isBodyRelated(CachedBody body) {
        if (this.selectionState != GuiCelestialSelection.EnumSelection.ZOOMED || this.selectedBody == null) return true;
        if (body.nativeBodyReference == this.selectedBody) return true;
        if (body.isMoon && body.parentPlanet != null && body.parentPlanet.nativeBodyReference == this.selectedBody) return true;
        if (!body.isMoon && this.selectedBody instanceof Moon) {
            Moon selectedMoon = (Moon) this.selectedBody;
            if (body.nativeBodyReference == selectedMoon.getParentPlanet()) return true;
        }
        return false;
    }

    private boolean isRayIntersectingSphere(Vec3d origin, Vec3d dir, Vec3d center, float radius) {
        Vec3d oc = origin.subtract(center);
        double a = dir.dotProduct(dir);
        double b = 2.0 * oc.dotProduct(dir);
        double c = oc.dotProduct(oc) - radius * radius;
        return (b * b - 4 * a * c) > 0;
    }

    // GL state managers and rendering

    private void setup3DViewport() {
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float aspectRatio = (float) this.width / (float) this.height;
        Project.gluPerspective(60.0f, aspectRatio, 0.1f, 100000.0f);

        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        GlStateManager.enableDepth();
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);

        float cameraDistance = BASE_CAMERA_DISTANCE / currentZoom;
        
        GlStateManager.translate(0.0f, 0.0f, -cameraDistance);
        GlStateManager.rotate(this.currentPitch, 1.0f, 0.0f, 0.0f); 
        GlStateManager.translate(-cameraX, 0.0f, -cameraZ);

        savedModelMatrix.clear();
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, savedModelMatrix);
        savedProjMatrix.clear();
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, savedProjMatrix);
        savedViewport.clear();
        GL11.glGetInteger(GL11.GL_VIEWPORT, savedViewport);
    }

    private void restore2DViewport() {
        GlStateManager.disableDepth();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
    }

    private void renderSystem(CachedSystem system, float partialTicks) {
        if (system == null) return;

        float globalTime = this.mc.world.getTotalWorldTime() + partialTicks;
        float starAlpha = (system.star != null && isBodyRelated(system.star)) ? 1.0f : this.unrelatedAlpha;

        if (starAlpha > 0.01f && (this.currentViewLevel != ViewLevel.LOCAL_PLANET || this.selectedBody == system.star.nativeBodyReference)) {
            GlStateManager.pushMatrix();
            if (system.star != null) {
                this.mc.getTextureManager().bindTexture(system.star.texture);
            } else {
                this.mc.getTextureManager().bindTexture(new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/sun.png"));
            }
            GlStateManager.rotate(globalTime * 0.5f, 0.0f, 1.0f, 0.0f); 
            
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, starAlpha);
            
            this.drawTexturedCube(16.0f);
            
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        CelestialBody activePlanetRef = this.selectedBody;
        if (this.selectedBody instanceof Moon) {
            activePlanetRef = ((Moon) this.selectedBody).getParentPlanet();
        }

        for (CachedBody planet : system.planets) {
            
            if (this.currentViewLevel == ViewLevel.LOCAL_PLANET && this.selectedBody != null && planet.nativeBodyReference != activePlanetRef) {
                continue;
            }
            
            this.renderCelestialBody(planet, globalTime, 5.0f, 1.8f, 30.0f); 
        }
    }

    private void renderCelestialBody(CachedBody body, float globalTime, float bodyScale, float moonScale, float orbitMultiplier) {
        float bodyAlpha = isBodyRelated(body) ? 1.0f : this.unrelatedAlpha;
        
        if (body.isMoon) {
            if (!this.showMoonsMacro) {
                boolean isChildMoon = (body.parentPlanet != null && this.selectedBody == body.parentPlanet.nativeBodyReference);
                if (isChildMoon || body.nativeBodyReference == this.selectedBody) {
                    bodyAlpha *= this.moonFadeAlpha; 
                } else {
                    bodyAlpha = 0.0f; 
                }
            }
        }
        
        float appliedOrbitMultiplier = orbitMultiplier;
        if (!body.isStar && !body.isMoon) {
            appliedOrbitMultiplier *= this.currentMacroScale;
        }

        float theta = ((globalTime / body.orbitTime) * ORBIT_SPEED_MULTIPLIER) + body.phaseShift;
        float scaledDistance = body.orbitDistance * appliedOrbitMultiplier;

        float posX = (float) Math.cos(theta) * scaledDistance;
        float posZ = (float) Math.sin(theta) * scaledDistance;

        boolean shouldDrawOrbit = true;
        if (body.isMoon) {
            shouldDrawOrbit = (body.parentPlanet != null && this.selectedBody == body.parentPlanet.nativeBodyReference) || (this.selectedBody == body.nativeBodyReference);
        } else if (!body.isStar && this.currentViewLevel == ViewLevel.LOCAL_PLANET) {
            shouldDrawOrbit = false;
        }
        
        if (body.isAsteroidBelt) {
            shouldDrawOrbit = false;
        }

        GlStateManager.pushMatrix();

        if (body.isMoon && this.currentOrbitTilt > 0.1f) {
            GlStateManager.rotate(-this.currentOrbitTilt, 1.0f, 0.0f, 0.0f);
            GlStateManager.rotate(this.currentOrbitTilt * 0.7f, 0.0f, 0.0f, 1.0f);
        }

        if (shouldDrawOrbit && bodyAlpha > 0.01f) {
            this.drawOrbitRing(scaledDistance, bodyAlpha);
        }

        if (body.isAsteroidBelt && this.showZonesMacro && bodyAlpha > 0.01f) {
            this.drawAsteroidBeltZone(scaledDistance, body.beltWidth * this.currentMacroScale, bodyAlpha, body.isKuiperBelt);
            
            List<CachedAsteroid> beltRocks = this.beltPopulations.get(body);
            if (beltRocks != null && !beltRocks.isEmpty()) {
                this.mc.getTextureManager().bindTexture(ASTEROID_TEXTURE);
                
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.color(0.8f, 0.8f, 0.8f, bodyAlpha);
                
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                for (CachedAsteroid ast : beltRocks) {
                    ast.orbitAngle += ast.orbitSpeed;
                    ast.pitch += ast.rotSpeedPitch;
                    ast.yaw += ast.rotSpeedYaw;
                    ast.roll += ast.rotSpeedRoll;

                    float r = scaledDistance + (ast.orbitRadiusOffset * this.currentMacroScale);
                    float astX = (float) Math.cos(ast.orbitAngle) * r;
                    float astZ = (float) Math.sin(ast.orbitAngle) * r;
                    float astY = ast.orbitYOffset * this.currentMacroScale;

                    GlStateManager.pushMatrix();
                    GlStateManager.translate(astX, astY, astZ);
                    GlStateManager.rotate(ast.yaw, 0, 1, 0);
                    GlStateManager.rotate(ast.pitch, 1, 0, 0);
                    GlStateManager.rotate(ast.roll, 0, 0, 1);
                    GlStateManager.scale(ast.scale, ast.scale, ast.scale);

                    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                    for (int i = 0; i < ast.vertices.length; i++) {
                        buffer.pos(ast.vertices[i][0], ast.vertices[i][1], ast.vertices[i][2])
                              .tex(ast.vertices[i][3], ast.vertices[i][4])
                              .endVertex();
                    }
                    tessellator.draw();
                    GlStateManager.popMatrix();
                }
                
                GlStateManager.disableBlend();
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            }
        }

        GlStateManager.translate(posX, 0.0f, posZ);

        if (bodyAlpha > 0.01f && !body.isAsteroidBelt) {
            GlStateManager.pushMatrix();
            this.mc.getTextureManager().bindTexture(body.texture);
            
            float spinSpeed = 5.0f; 
            GlStateManager.rotate(globalTime * spinSpeed, 0.0f, 1.0f, 0.0f);
            
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.color(1.0f, 1.0f, 1.0f, bodyAlpha);
            
            this.drawTexturedCube(bodyScale);
            
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        boolean renderMoons = this.showMoonsMacro || this.selectedBody != null;
        if (!this.showMoonsMacro && this.selectedBody != null) {
            boolean isParentOfSelected = false;
            if (this.selectedBody instanceof Moon) {
                isParentOfSelected = (body.nativeBodyReference == ((Moon) this.selectedBody).getParentPlanet());
            }
            if (!body.isMoon && body.nativeBodyReference != this.selectedBody && !isParentOfSelected) {
                renderMoons = false; 
            }
        }

        if (renderMoons) {
            for (CachedBody moon : body.moons) {
                this.renderCelestialBody(moon, globalTime, moonScale, moonScale * 0.4f, 1.5f);
            }
        }

        GlStateManager.popMatrix();
    }

    private void drawAsteroidBeltZone(float radius, float width, float alpha, boolean isKuiper) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        
        GlStateManager.disableCull(); 
        GlStateManager.disableAlpha(); 
        GlStateManager.shadeModel(GL11.GL_SMOOTH); 
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        float halfWidth = width / 2.0f;
        int segments = 64;
        
        int r = isKuiper ? 30 : 255;
        int g = isKuiper ? 100 : 45;
        int b = isKuiper ? 255 : 45;
        
        int centerAlpha = (int)(110 * alpha); 
        int edgeAlpha = 0;                    
        
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            
            buffer.pos(cos * (radius - halfWidth), 0, sin * (radius - halfWidth)).color(r, g, b, edgeAlpha).endVertex();
            buffer.pos(cos * radius, 0, sin * radius).color(r, g, b, centerAlpha).endVertex();
        }
        tessellator.draw();
        
        buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);
            
            buffer.pos(cos * radius, 0, sin * radius).color(r, g, b, centerAlpha).endVertex();
            buffer.pos(cos * (radius + halfWidth), 0, sin * (radius + halfWidth)).color(r, g, b, edgeAlpha).endVertex();
        }
        tessellator.draw();
        
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.enableAlpha(); 
        GlStateManager.enableCull(); 
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private void drawOrbitRing(float radius, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glLineWidth(1.5F);

        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color(0.2f, 0.6f, 1.0f, 0.4f * alpha);
        
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);
        
        int segments = 64; 
        for (int i = 0; i < segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float x = (float) Math.cos(angle) * radius;
            float z = (float) Math.sin(angle) * radius;
            buffer.pos(x, 0, z).endVertex();
        }
        
        tessellator.draw();
        
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    private void drawTexturedCube(float size) {
        float half = size / 2.0f;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        buffer.pos(-half, half, -half).tex(0, 0).endVertex();
        buffer.pos(-half, half,  half).tex(0, 1).endVertex();
        buffer.pos( half, half,  half).tex(1, 1).endVertex();
        buffer.pos( half, half, -half).tex(1, 0).endVertex();

        buffer.pos( half, -half, -half).tex(1, 0).endVertex();
        buffer.pos( half, -half,  half).tex(1, 1).endVertex();
        buffer.pos(-half, -half,  half).tex(0, 1).endVertex();
        buffer.pos(-half, -half, -half).tex(0, 0).endVertex();

        buffer.pos(-half,  half, -half).tex(0, 0).endVertex();
        buffer.pos( half,  half, -half).tex(1, 0).endVertex();
        buffer.pos( half, -half, -half).tex(1, 1).endVertex();
        buffer.pos(-half, -half, -half).tex(0, 1).endVertex();

        buffer.pos( half,  half,  half).tex(0, 0).endVertex();
        buffer.pos(-half,  half,  half).tex(1, 0).endVertex();
        buffer.pos(-half, -half,  half).tex(1, 1).endVertex();
        buffer.pos( half, -half,  half).tex(0, 1).endVertex();

        buffer.pos(-half,  half,  half).tex(0, 0).endVertex();
        buffer.pos(-half,  half, -half).tex(1, 0).endVertex();
        buffer.pos(-half, -half, -half).tex(1, 1).endVertex();
        buffer.pos(-half, -half,  half).tex(0, 1).endVertex();

        buffer.pos( half,  half, -half).tex(0, 0).endVertex();
        buffer.pos( half,  half,  half).tex(1, 0).endVertex();
        buffer.pos( half, -half,  half).tex(1, 1).endVertex();
        buffer.pos( half, -half, -half).tex(0, 1).endVertex();

        tessellator.draw();
    }

    private CachedBody getCachedBodyForNative(CelestialBody nativeBody) {
        if (nativeBody == null) return null;
        String searchName = nativeBody.getName();

        if (this.activeSystem != null) {
            if (this.activeSystem.star != null && this.activeSystem.star.name.equals(searchName)) return this.activeSystem.star;
            for (CachedBody planet : this.activeSystem.planets) {
                if (planet.name.equals(searchName)) return planet;
                for (CachedBody moon : planet.moons) {
                    if (moon.name.equals(searchName)) return moon;
                }
            }
        }

        for (CachedSystem sys : this.systemCache.values()) {
            if (sys.star != null && sys.star.name.equals(searchName)) {
                switchSystem(sys);
                return sys.star;
            }
            for (CachedBody planet : sys.planets) {
                if (planet.name.equals(searchName)) {
                    switchSystem(sys);
                    return planet;
                }
                for (CachedBody moon : planet.moons) {
                    if (moon.name.equals(searchName)) {
                        switchSystem(sys);
                        return moon;
                    }
                }
            }
        }
        return null;
    }

    // State machine and data classes

    public enum ViewLevel {
        GALAXY,
        SOLAR_SYSTEM,
        LOCAL_PLANET
    }

    public static class GalaxyParticle {
        public final float x, y, z;
        public final float r, g, b, alpha;
        public final float size;

        public GalaxyParticle(float x, float y, float z, float r, float g, float b, float alpha, float size) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.r = Math.max(0.0f, Math.min(1.0f, r));
            this.g = Math.max(0.0f, Math.min(1.0f, g));
            this.b = Math.max(0.0f, Math.min(1.0f, b));
            this.alpha = alpha;
            this.size = size;
        }
    }

    public static class CachedSystem {
        public final String name;
        public final SolarSystem nativeSystemReference;
        public CachedBody star;
        public final List<CachedBody> planets = new ArrayList<>();

        // Galaxy Positioning & Projection States
        public float galaxyX, galaxyZ;
        public float projectedX, projectedY;
        public float textX, textY, textW, textH;
        public boolean isVisible;
        public boolean isHovered;

        public CachedSystem(String name, SolarSystem sysRef) {
            this.name = (name != null) ? name : "Unknown System";
            this.nativeSystemReference = sysRef;

            Random r = new Random(this.name.hashCode());
            float angle = r.nextFloat() * (float)Math.PI * 2.0f;
            float dist = 80.0f + r.nextFloat() * 250.0f; 
            
            String search = this.name.toLowerCase();
            
            // Substring mapping collision fix applied here using exact .equals for Sol
            if (search.equals("sol") || search.equals("solar system")) { angle = 1.0f; dist = 140.0f; }
            else if (search.contains("triopas")) { angle = 2.4f; dist = 180.0f; }
            else if (search.contains("kepler 69") || search.contains("keplar 69")) { angle = 1.8f; dist = 260.0f; }
            else if (search.contains("kepler 22") || search.contains("keplar 22")) { angle = 0.2f; dist = 220.0f; }
            else if (search.contains("kepler 62") || search.contains("keplar 62")) { angle = -1.2f; dist = 280.0f; }
            else if (search.contains("kepler 47") || search.contains("keplar 47")) { angle = 3.5f; dist = 240.0f; }
            else if (search.contains("artemis")) { angle = 0.0f; dist = 0.0f; }
            else if (search.contains("epsilon") || search.contains("epsilum")) { angle = 3.1f; dist = 60.0f; }
            else if (search.contains("xenos")) { angle = 4.5f; dist = 320.0f; }
            else if (search.contains("venarizi") || search.contains("vendrizi")) { angle = 5.2f; dist = 200.0f; }
            
            this.galaxyX = (float)Math.cos(angle) * dist;
            this.galaxyZ = (float)Math.sin(angle) * dist;
        }
    }

    public static class CachedBody {
        public final String name;
        public final CelestialBody nativeBodyReference;
        public final ResourceLocation texture;
        public final float phaseShift;
        public final float orbitDistance;
        public final float orbitTime;
        public final List<CachedBody> moons = new ArrayList<>();
        
        public final boolean isMoon;
        public final boolean isStar;
        public final boolean isAsteroidBelt; 
        public final boolean isKuiperBelt; 
        public final float beltWidth; 
        public final CachedBody parentPlanet;

        public CachedBody(CelestialBody body, boolean isMoon, boolean isStar, CachedBody parentPlanet) {
            this.name = (body.getName() != null) ? body.getName() : "Unknown Body";
            this.nativeBodyReference = body;
            this.isMoon = isMoon;
            this.isStar = isStar;
            this.parentPlanet = parentPlanet;
            
            String lowerName = this.name.toLowerCase();
            this.isKuiperBelt = lowerName.contains("kuiper");
            this.isAsteroidBelt = lowerName.contains("asteroid") || this.isKuiperBelt;
            
            if (this.isKuiperBelt) {
                this.beltWidth = 55.0f; 
            } else if (this.isAsteroidBelt) {
                this.beltWidth = 25.0f; 
            } else {
                this.beltWidth = 0.0f;
            }

            this.texture = (body.getBodyIcon() != null) 
                    ? body.getBodyIcon() 
                    : new ResourceLocation("galacticraftcore", "textures/gui/celestialbodies/unknown.png");

            this.orbitDistance = (body.getRelativeDistanceFromCenter() != null) 
                    ? body.getRelativeDistanceFromCenter().unScaledDistance 
                    : 1.0f;

            float rawPhase = body.getPhaseShift();
            this.phaseShift = Float.isNaN(rawPhase) ? 0.0f : rawPhase;

            float rawOrbitTime = body.getRelativeOrbitTime();
            this.orbitTime = (rawOrbitTime == 0.0f || Float.isNaN(rawOrbitTime)) ? 1.0f : rawOrbitTime;
        }
    }

    public static class CachedAsteroid {
        public float x, y, z;
        public float vx, vy, vz;
        public float pitch, yaw, roll;
        public float rotSpeedPitch, rotSpeedYaw, rotSpeedRoll;
        public float scale;
        
        public int age;
        public int maxAge;
        public float fadeAlpha;

        public boolean isBeltAsteroid;
        public float orbitAngle;
        public float orbitRadiusOffset;
        public float orbitYOffset;
        public float orbitSpeed;

        public float[][] vertices = new float[24][5];

        public CachedAsteroid(Random rand) {
            this.generateJaggedGeometry(rand);
        }

        public static CachedAsteroid createBeltAsteroid(Random rand, float beltWidth) {
            CachedAsteroid ast = new CachedAsteroid(rand);
            ast.isBeltAsteroid = true;
            ast.orbitAngle = rand.nextFloat() * (float)Math.PI * 2.0f;
            
            ast.orbitRadiusOffset = (rand.nextFloat() - 0.5f) * beltWidth;
            ast.orbitYOffset = (rand.nextFloat() - 0.5f) * (beltWidth * 0.15f); 
            ast.orbitSpeed = -(0.0005f + rand.nextFloat() * 0.0015f); 
            
            ast.scale = 0.3f + (rand.nextFloat() * 0.5f); 
            
            ast.pitch = rand.nextFloat() * 360.0f;
            ast.yaw = rand.nextFloat() * 360.0f;
            ast.roll = rand.nextFloat() * 360.0f;
            
            ast.rotSpeedPitch = (rand.nextFloat() - 0.5f) * 3.0f;
            ast.rotSpeedYaw = (rand.nextFloat() - 0.5f) * 3.0f;
            ast.rotSpeedRoll = (rand.nextFloat() - 0.5f) * 3.0f;
            
            return ast;
        }

        public void respawn(Random rand, CachedAsteroid clusterTarget) {
            this.age = 0;
            this.maxAge = 600 + rand.nextInt(1200); 
            this.fadeAlpha = 0.0f;
            this.scale = 0.5f + (rand.nextFloat() * 0.8f);

            if (clusterTarget != null) {
                this.x = clusterTarget.x + (rand.nextFloat() - 0.5f) * 20.0f;
                this.y = clusterTarget.y + (rand.nextFloat() - 0.5f) * 20.0f;
                this.z = clusterTarget.z + (rand.nextFloat() - 0.5f) * 20.0f;
            } else {
                float randVal = rand.nextFloat();
                float radius = 30.0f + (float)Math.pow(randVal, 0.5) * 850.0f; 
                float angle = rand.nextFloat() * (float)Math.PI * 2.0f;
                
                this.x = (float)Math.cos(angle) * radius;
                this.z = (float)Math.sin(angle) * radius;
                this.y = (rand.nextFloat() * 60.0f) - 30.0f;
            }

            this.vx = (rand.nextFloat() - 0.5f) * 0.008f;
            this.vy = (rand.nextFloat() - 0.5f) * 0.003f;
            this.vz = (rand.nextFloat() - 0.5f) * 0.008f;

            this.pitch = rand.nextFloat() * 360.0f;
            this.yaw = rand.nextFloat() * 360.0f;
            this.roll = rand.nextFloat() * 360.0f;

            this.rotSpeedPitch = (rand.nextFloat() - 0.5f) * 2.0f;
            this.rotSpeedYaw = (rand.nextFloat() - 0.5f) * 2.0f;
            this.rotSpeedRoll = (rand.nextFloat() - 0.5f) * 2.0f;
        }

        private void generateJaggedGeometry(Random rand) {
            float[][] corners = new float[8][3];
            for (int i = 0; i < 8; i++) {
                float bx = (i & 1) == 0 ? -0.5f : 0.5f;
                float by = (i & 2) == 0 ? -0.5f : 0.5f;
                float bz = (i & 4) == 0 ? -0.5f : 0.5f;
                
                float distMult = 0.4f + (rand.nextFloat() * 1.2f);
                corners[i][0] = bx * distMult;
                corners[i][1] = by * distMult;
                corners[i][2] = bz * distMult;
            }

            int[][] faceIndices = {
                {0, 1, 5, 4}, 
                {2, 3, 7, 6}, 
                {0, 1, 3, 2}, 
                {4, 5, 7, 6}, 
                {0, 4, 6, 2}, 
                {1, 5, 7, 3}  
            };

            float[][] uvs = {
                {0, 0}, {1, 0}, {1, 1}, {0, 1}
            };

            int vIndex = 0;
            for (int face = 0; face < 6; face++) {
                for (int corner = 0; corner < 4; corner++) {
                    int pIdx = faceIndices[face][corner];
                    vertices[vIndex][0] = corners[pIdx][0]; 
                    vertices[vIndex][1] = corners[pIdx][1]; 
                    vertices[vIndex][2] = corners[pIdx][2]; 
                    vertices[vIndex][3] = uvs[corner][0];   
                    vertices[vIndex][4] = uvs[corner][1];   
                    vIndex++;
                }
            }
        }
    }
}