package me.numin.spirits.ability.dark.combo;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import java.util.ArrayList;
import java.util.Random;
import me.numin.spirits.Spirits;
import me.numin.spirits.ability.api.DarkAbility;
import me.numin.spirits.utilities.Methods;
import me.numin.spirits.utilities.Methods.SpiritType;
import me.numin.spirits.utilities.Removal;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Infest extends DarkAbility implements AddonAbility, ComboAbility {

  //TODO: Add sounds.

  private Location circleCenter, location, location2, location3;
  private Removal removal;

  private boolean damageEntities, healDarkSpirits;
  private double damage, radius, t;
  private int currPoint, effectInt;
  private long cooldown, duration, time;

  public Infest(Player player) {
    super(player);

    if (!bPlayer.canBendIgnoreBinds(this)) {
      return;
    }
    setFields();
    time = System.currentTimeMillis();
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3F, -1);
    start();
    bPlayer.addCooldown(this);
  }

  private void setFields() {
    this.cooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.DarkSpirit.Combo.Infest.Cooldown");
    this.duration = Spirits.plugin.getConfig().getLong("Abilities.Spirits.DarkSpirit.Combo.Infest.Duration");
    this.radius = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.DarkSpirit.Combo.Infest.Radius");
    this.effectInt = Spirits.plugin.getConfig().getInt("Abilities.Spirits.DarkSpirit.Combo.Infest.EffectInterval");
    this.damage = Spirits.plugin.getConfig().getInt("Abilities.Spirits.DarkSpirit.Combo.Infest.Damage");
    this.damageEntities = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.DarkSpirit.Combo.Infest.DamageEntities");
    this.healDarkSpirits = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.DarkSpirit.Combo.Infest.HealDarkSpirits");
    location = player.getLocation();
    location2 = player.getLocation();
    location3 = player.getLocation();
    circleCenter = player.getLocation();
    this.removal = new Removal(player);
  }

  @Override
  public void progress() {
    if (removal.stop()) {
      remove();
      return;
    }
    if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
      remove();
      return;
    }
    if (System.currentTimeMillis() > time + duration) {
      remove();
      return;
    }
    spawnCircle();
    grabEntities();
  }

  private void spawnCircle() {
    Methods.createPolygon(location, 8, radius, 0.2, Particle.SPELL_WITCH);
    for (int i = 0; i < 6; i++) {
      this.currPoint += 360 / 300;
      if (this.currPoint > 360) {
        this.currPoint = 0;
      }
      double angle = this.currPoint * Math.PI / 180.0D;
      double x = radius * Math.cos(angle);
      double x2 = radius * Math.sin(angle);
      double z = radius * Math.sin(angle);
      double z2 = radius * Math.cos(angle);
      location2.add(x, 0, z);
      ParticleEffect.SMOKE_NORMAL.display(location2, 1, 0, 0, 0, 0);
      location2.subtract(x, 0, z);

      location3.add(x2, 0, z2);
      ParticleEffect.SMOKE_NORMAL.display(location3, 1, 0, 0, 0, 0);
      location3.subtract(x2, 0, z2);
    }
    t += Math.PI / 32;
    if (!(t >= Math.PI * 4)) {
      for (double i = 0; i <= Math.PI * 2; i += Math.PI / 1.2) {
        double x = 0.5 * (Math.PI * 4 - t) * Math.cos(t - i);
        double y = 0.4 * t;
        double z = 0.5 * (Math.PI * 4 - t) * Math.sin(t - i);
        location.add(x, y, z);
        Methods.playSpiritParticles(SpiritType.DARK, location, 0, 0, 0, 0, 1);
        player.getWorld().spawnParticle(Particle.REDSTONE, location, 1, 0.1, 0.1, 0.1, 0, new DustOptions(Color.fromBGR(100, 100, 100), 1));
        location.subtract(x, y, z);
      }
    }

    player.getWorld().spawnParticle(Particle.TOWN_AURA, location, 10, radius / 2, 0.6, radius / 2, 0);
  }

  private void grabEntities() {
    for (Entity entity : GeneralMethods.getEntitiesAroundPoint(circleCenter, radius)) {
      if (entity instanceof LivingEntity) {
        infestEntities(entity);
      }
    }
  }

  private void infestEntities(Entity entity) {
    if (new Random().nextInt(effectInt) == 0) {
      if (entity instanceof Player) {
        Player ePlayer = (Player) entity;
        BendingPlayer bEntity = BendingPlayer.getBendingPlayer(ePlayer);
        if (bEntity.hasElement(Element.getElement("DarkSpirit"))) {
          if (healDarkSpirits) {
            LivingEntity le = (LivingEntity) entity;
            le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 1));
            ParticleEffect.HEART.display(entity.getLocation().add(0, 2, 0), 1, 0, 0, 0, 0);
          }
        } else {
          DamageHandler.damageEntity(entity, damage, this);
          ParticleEffect.PORTAL.display(entity.getLocation().add(0, 1, 0), 0, 0, 0, 1.5F, 5);
        }

      } else if (entity instanceof Monster) {
        LivingEntity le = (LivingEntity) entity;
        le.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 1));
        ParticleEffect.VILLAGER_ANGRY.display(entity.getLocation().add(0, 1, 0), 1, 0, 0, 0, 0);
      } else if (entity instanceof LivingEntity && damageEntities) {
        DamageHandler.damageEntity(entity, damage, this);
        ParticleEffect.PORTAL.display(entity.getLocation().add(0, 1, 0), 0, 0, 0, 1.5F, 5);

      }
    }
  }

  @Override
  public Object createNewComboInstance(Player player) {
    return new Infest(player);
  }

  @Override
  public ArrayList<AbilityInformation> getCombination() {
    ArrayList<AbilityInformation> combo = new ArrayList<AbilityInformation>();
    combo.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_DOWN));
    combo.add(new AbilityInformation("Intoxicate", ClickType.RIGHT_CLICK_BLOCK));
    combo.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_UP));
    return combo;
  }

  @Override
  public long getCooldown() {
    return cooldown;
  }

  @Override
  public Location getLocation() {
    return location;
  }

  @Override
  public double getCollisionRadius() {
    return radius;
  }

  @Override
  public String getName() {
    return "Infest";
  }

  @Override
  public String getDescription() {
    return Methods.setSpiritDescription(SpiritType.DARK, "Combo") +
        Spirits.plugin.getConfig().getString("Language.Abilities.DarkSpirit.Infest.Description");
  }

  @Override
  public String getInstructions() {
    return Methods.getSpiritColor(SpiritType.DARK) +
        Spirits.plugin.getConfig().getString("Language.Abilities.DarkSpirit.Infest.Instructions");
  }

  @Override
  public String getAuthor() {
    return Methods.getSpiritColor(SpiritType.DARK) + "" + Methods.getAuthor();
  }

  @Override
  public String getVersion() {
    return Methods.getSpiritColor(SpiritType.DARK) + Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.DarkSpirit.Combo.Infest.Enabled");
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
    return false;
  }

  @Override
  public void load() {
  }

  @Override
  public void stop() {
  }
}