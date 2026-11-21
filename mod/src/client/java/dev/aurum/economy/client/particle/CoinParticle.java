package dev.aurum.economy.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

/**
 * Золотая монета: вылетает вверх, крутится вокруг вертикальной оси (8 кадров),
 * падает под действием гравитации и плавно растворяется.
 */
public class CoinParticle extends SpriteBillboardParticle {
	private final SpriteProvider sprites;
	private final int spinOffset;

	private final boolean isFlipped;

	protected CoinParticle(ClientWorld world, double x, double y, double z,
						   double vx, double vy, double vz, SpriteProvider sprites) {
		super(world, x, y, z);
		this.sprites = sprites;
		this.velocityX = vx;
		this.velocityY = vy;
		this.velocityZ = vz;
		this.gravityStrength = 1.0f;
		this.velocityMultiplier = 0.97f;
		this.collidesWithWorld = true;
		this.maxAge = 34 + this.random.nextInt(12);
		this.isFlipped = Math.abs(vy - 0.44) < 0.01;
		this.scale = isFlipped ? 0.21f : (0.085f + this.random.nextFloat() * 0.03f);
		this.spinOffset = this.random.nextInt(8);
		this.setSprite(sprites.getSprite(spinOffset, 7));
	}

	@Override
	public void tick() {
		super.tick();
		if (this.dead) return;

		// Вращение: меняем кадр каждые 2 тика, для подброшенной монеты - каждый тик
		int frame = (this.age / (isFlipped ? 1 : 2) + spinOffset) % 8;
		this.setSprite(sprites.getSprite(frame, 7));

		// Плавное исчезновение в последние 10 тиков
		int left = this.maxAge - this.age;
		this.alpha = left < 10 ? Math.max(0f, left / 10f) : 1f;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getBrightness(float tint) {
		// Золото светится: яркость не ниже 0xB0
		int base = super.getBrightness(tint);
		int block = Math.max(base & 0xFF, 0xB0);
		int sky = (base >> 16) & 0xFF;
		return (sky << 16) | block;
	}

	public static class Factory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider sprites;

		public Factory(SpriteProvider sprites) {
			this.sprites = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientWorld world,
									   double x, double y, double z, double vx, double vy, double vz) {
			return new CoinParticle(world, x, y, z, vx, vy, vz, sprites);
		}
	}
}
