package me.numin.spirits.ability.dark;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.util.DamageHandler;
import java.util.Random;
import me.numin.spirits.Methods;
import me.numin.spirits.Methods.SpiritType;
import me.numin.spirits.Spirits;
import me.numin.spirits.ability.api.DarkAbility;
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

public class Intoxicate extends DarkAbility implements AddonAbility {

  LivingEntity target = null;
  private Location location;
  private Location origin;
  private Location entityCheck;
  private Vector direction;
  private int currPoint;
  private int red, green, blue;
  private double range;
  private double selfDamage;
  private long time;
  private long potInt;
  private long harmInt;
  private long cooldown;
  private boolean progress;

  public Intoxicate(Player player) {
    super(player);

    if (!bPlayer.canBend(this)) {
      return;
    }

    setFields();
    time = System.currentTimeMillis();
    start();
  }

  private void setFields() {
    this.cooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.DarkSpirit.Intoxicate.Cooldown");
    this.range = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.DarkSpirit.Intoxicate.Radius");
    this.potInt = Spirits.plugin.getConfig().getLong("Abilities.Spirits.DarkSpirit.Intoxicate.PotionInterval");
    this.harmInt = Spirits.plugin.getConfig().getLong("Abilities.Spirits.DarkSpirit.Intoxicate.HarmInterval");
    this.selfDamage = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.DarkSpirit.Intoxicate.SelfDamage");

    this.red = Spirits.plugin.getConfig().getInt("Abilities.Spirits.DarkSpirit.Intoxicate.ParticleColor.Red");
    this.green = Spirits.plugin.getConfig().getInt("Abilities.Spirits.DarkSpirit.Intoxicate.ParticleColor.Green");
    this.blue = Spirits.plugin.getConfig().getInt("Abilities.Spirits.DarkSpirit.Intoxicate.ParticleColor.Blue");

    this.origin = player.getLocation().clone().add(0, 1, 0);
    this.location = origin.clone();
    this.direction = player.getLocation().getDirection();
    this.progress = true;
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
        //ParticleEffect.FLAME.display(entityCheck, 0, 0, 0, 0, 1);
      }
      if (origin.distanceSquared(entityCheck) > range * range) {
        progress = false;
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
        effect(200, 0.04F, target, target.getLocation().clone());
      }
    } else {
      remove();
      return;
    }
  }

  public void effect(int points, float size, Entity target, Location location) {
    Color color = Color.fromBGR(blue, green, red);
    DustOptions dustOptions = new DustOptions(color, 1);
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
      location.subtract(x, y, z);
    }

    if (System.currentTimeMillis() - time > potInt) {
      for (PotionEffect targetEffect : le.getActivePotionEffects()) {
        if (isPositiveEffect(targetEffect.getType())) {
          le.removePotionEffect(targetEffect.getType());
        }
      }
      bPlayer.addCooldown(this);
    }
    if (System.currentTimeMillis() - time > harmInt) {
      le.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 1), true);
      le.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, 1000, 1), true);
      le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 300, 1), true);
      DamageHandler.damageEntity(player, selfDamage, this);
      bPlayer.addCooldown(this);
      remove();
      return;
    }
    if (new Random().nextInt(20) == 0) {
      player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1, -1);
    }
  }

  @Override
  public long getCooldown() {
    return cooldown;
  }

  @Override
  public Location getLocation() {
    return null;
  }

  @Override
  public String getName() {
    return "Intoxicate";
  }

  @Override
  public String getDescription() {
    return Methods.setSpiritDescription(SpiritType.DARK, "Offense") +
        Spirits.plugin.getConfig().getString("Language.Abilities.DarkSpirit.Intoxicate.Description");
  }

  @Override
  public String getInstructions() {
    return Methods.setSpiritDescriptionColor(SpiritType.DARK) +
        Spirits.plugin.getConfig().getString("Language.Abilities.DarkSpirit.Intoxicate.Instructions");
  }

  @Override
  public String getAuthor() {
    return Methods.setSpiritDescriptionColor(SpiritType.DARK) + Methods.getAuthor();
  }

  @Override
  public String getVersion() {
    return Methods.setSpiritDescriptionColor(SpiritType.DARK) + Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.DarkSpirit.Intoxicate.Enabled");
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