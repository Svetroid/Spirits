package me.numin.spirits.ability.light;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import java.util.Random;
import me.numin.spirits.Methods;
import me.numin.spirits.Methods.SpiritType;
import me.numin.spirits.Spirits;
import me.numin.spirits.ability.api.LightAbility;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Alleviate extends LightAbility implements AddonAbility {

  private LivingEntity target = null;
  private Location location;
  private Location origin;
  private Location entityCheck;
  private Vector direction;
  private int currPoint;
  private int healDuration;
  private int nightVisDuration;
  private int red, green, blue;
  private double range;
  private double selfDamage;
  private long time;
  private long potInt;
  private long healInt;
  private long otherCooldown;
  private long selfCooldown;
  private long chargeTime;
  private boolean progress;
  private boolean playingAlleviate;
  private boolean removeNegPots;

  public Alleviate(Player player) {
    super(player);

    if (!bPlayer.canBend(this)) {
      return;
    }

    setFields();
    time = System.currentTimeMillis();
    start();
  }

  private void setFields() {
    //Alleviate
    this.otherCooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.LightSpirit.Alleviate.Others.Cooldown");
    this.range = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.LightSpirit.Alleviate.Others.Range");
    this.potInt = Spirits.plugin.getConfig().getLong("Abilities.Spirits.LightSpirit.Alleviate.Others.PotionInterval");
    this.healInt = Spirits.plugin.getConfig().getLong("Abilities.Spirits.LightSpirit.Alleviate.Others.HealInterval");
    this.selfDamage = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.LightSpirit.Alleviate.Others.SelfDamage");

    //Sanctity
    this.selfCooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.LightSpirit.Alleviate.Self.Cooldown");
    this.chargeTime = Spirits.plugin.getConfig().getLong("Abilities.Spirits.LightSpirit.Alleviate.Self.ChargeTime");
    this.healDuration = Spirits.plugin.getConfig().getInt("Abilities.Spirits.LightSpirit.Alleviate.Self.HealDuration");
    this.nightVisDuration = Spirits.plugin.getConfig().getInt("Abilities.Spirits.LightSpirit.Alleviate.Self.NightVisionDuration");
    this.removeNegPots = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.LightSpirit.Alleviate.Self.RemoveNegativePotionEffects");

    this.red = Spirits.plugin.getConfig().getInt("Abilities.Spirits.LightSpirit.Alleviate.ParticleColor.Red");
    this.green = Spirits.plugin.getConfig().getInt("Abilities.Spirits.LightSpirit.Alleviate.ParticleColor.Green");
    this.blue = Spirits.plugin.getConfig().getInt("Abilities.Spirits.LightSpirit.Alleviate.ParticleColor.Blue");

    this.origin = player.getLocation().clone().add(0, 1, 0);
    this.location = origin.clone();
    this.direction = player.getLocation().getDirection();
    this.progress = true;
    this.playingAlleviate = false;
  }

  @Override
  public void progress() {
    if (player.isDead() || !player.isOnline() || GeneralMethods.isRegionProtectedFromBuild(this, location)) {
      remove();
      return;
    }

    if (!bPlayer.getBoundAbilityName().equals(getName())) {
      remove();
      return;
    }
    if (player.isSneaking()) {
      if (progress) {
        entityCheck = location;
        entityCheck.add(direction.multiply(1));
        // ParticleEffect.FLAME.display(location, 0, 0, 0, 0, 1);
      }
      if (origin.distanceSquared(entityCheck) > range * range) {
        progress = false;
        progressSanctity(200, 0.04F);
      }
      if (target == null) {
        for (Entity entity : GeneralMethods.getEntitiesAroundPoint(entityCheck, 1)) {
          if ((entity instanceof LivingEntity) && entity.getUniqueId() != player.getUniqueId()) {
            target = (LivingEntity) entity;
          }
        }
      } else {
        progress = false;
        entityCheck = target.getLocation();
        progressAlleviate(200, 0.04F, target, target.getLocation().clone());
      }
    } else {
      remove();
      return;
    }
  }

  public void progressAlleviate(int points, float size, Entity target, Location location) {
    Color color = Color.fromBGR(blue, green, red);
    DustOptions dustOptions = new DustOptions(color, 1);
    playingAlleviate = true;
    LivingEntity le = (LivingEntity) target;

    for (int i = 0; i < 6; i++) {
      currPoint += 360 / points;
      if (currPoint > 360) {
        currPoint = 0;
      }
      double angle = currPoint * Math.PI / 180 * Math.cos(Math.PI);
      double x = size * (Math.PI * 4 - angle) * Math.cos(angle + i);
      double y = 1.2 * Math.cos(angle) + 1.2;
      double z = size * (Math.PI * 4 - angle) * Math.sin(angle + i);
      location.add(x, y, z);
      player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, dustOptions);
      //GeneralMethods.displayColoredParticle(location, hexColor);
      location.subtract(x, y, z);
    }

    if (System.currentTimeMillis() - time > potInt) {
      for (PotionEffect targetEffect : le.getActivePotionEffects()) {
        if (isNegativeEffect(targetEffect.getType())) {
          le.removePotionEffect(targetEffect.getType());
        }
      }
      bPlayer.addCooldown(this, otherCooldown);
    }
    if (System.currentTimeMillis() - time > healInt) {
      le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 1), true);
      DamageHandler.damageEntity(player, selfDamage, this);
      bPlayer.addCooldown(this, otherCooldown);
      remove();
      return;
    }
    if (!player.isSneaking()) {
      bPlayer.addCooldown(this, otherCooldown);
    }
    if (new Random().nextInt(20) == 0) {
      target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1, 1);
    }
  }

  public void progressSanctity(int points, float size) {
    Color color = Color.fromBGR(blue, green, red);
    DustOptions dustOptions = new DustOptions(color, 1);

    if (playingAlleviate) {
      return;
    } else {
      Location location = player.getLocation();
      for (int i = 0; i < 6; i++) {
        currPoint += 360 / points;
        if (currPoint > 360) {
          currPoint = 0;
        }
        double angle = currPoint * Math.PI / 180 * Math.cos(Math.PI);
        double x = size * (Math.PI * 4 - angle) * Math.cos(angle + i);
        double y = 1.2 * Math.cos(angle) + 1.2;
        double z = size * (Math.PI * 4 - angle) * Math.sin(angle + i);
        location.add(x, y, z);
        player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0, 0, 0, 0, dustOptions);
        //GeneralMethods.displayColoredParticle(location, selfHexColor);
        location.subtract(x, y, z);
      }
      if (System.currentTimeMillis() > time + chargeTime) {
        for (PotionEffect playerEffects : player.getActivePotionEffects()) {
          if (isNegativeEffect(playerEffects.getType()) && removeNegPots) {
            player.removePotionEffect(playerEffects.getType());
          }
        }
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, healDuration * 100, 1), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, nightVisDuration * 100, 1));

        bPlayer.addCooldown(this, selfCooldown);
        remove();
        return;
      }
    }
    if (!player.isSneaking()) {
      bPlayer.addCooldown(this, selfCooldown);
    }
    if (new Random().nextInt(20) == 0) {
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1, 1);
    }
  }

  @Override
  public long getCooldown() {
    return 0;
  }

  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public String getName() {
    return "Alleviate";
  }

  @Override
  public String getDescription() {
    return Methods.setSpiritDescription(SpiritType.LIGHT, "Utility") +
        Spirits.plugin.getConfig().getString("Language.Abilities.LightSpirit.Alleviate.Description");
  }

  @Override
  public String getInstructions() {
    return Methods.setSpiritDescriptionColor(SpiritType.LIGHT) +
        Spirits.plugin.getConfig().getString("Language.Abilities.LightSpirit.Alleviate.Instructions");
  }

  @Override
  public String getAuthor() {
    return Methods.setSpiritDescriptionColor(SpiritType.LIGHT) + Methods.getAuthor();
  }

  @Override
  public String getVersion() {
    return Methods.setSpiritDescriptionColor(SpiritType.LIGHT) + Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.LightSpirit.Alleviate.Enabled");
  }

  @Override
  public boolean isExplosiveAbility() {
    return false;
  }

  @Override
  public boolean isHarmlessAbility() {
    return false;
  }

  @Override
  public boolean isIgniteAbility() {
    return false;
  }

  @Override
  public boolean isSneakAbility() {
    return true;
  }

  @Override
  public void load() {
  }

  @Override
  public void stop() {
  }
}