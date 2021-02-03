package me.numin.spirits.ability.water;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.util.MovementHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import me.numin.spirits.Methods;
import me.numin.spirits.SpiritElement;
import me.numin.spirits.Spirits;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Purify extends WaterAbility implements AddonAbility {

  public LivingEntity target;
  private double range;
  public static Set<Integer> heldEntities = new HashSet<Integer>();
  public byte stage = 0;
  public Location travelLoc = null;
  private long duration;
  public double yaw;
  public Random random;
  private long cooldown;
  private boolean hasReached = true;
  private int ticks;
  private int chargeTicks;
  private long time;
  private boolean charged = false;
  private boolean setElement;

  public Purify(Player player) {
    super(player);
    if (!bPlayer.canBend(this)) {
      return;
    }
    firstloop:
    for (int i = 20; i < 100; i++) {
      Location loc = GeneralMethods.getTargetedLocation(player, range);
      for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 10)) {
        if (e instanceof LivingEntity && e.getEntityId() != player.getEntityId()) {
          target = (LivingEntity) e;
          break firstloop;
        }
      }
    }
    time = System.currentTimeMillis();

    if (target == null) {
      return;
    }
    heldEntities.add(target.getEntityId());
    setFields();
    start();
  }

  private void setFields() {
    this.cooldown = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Water.Purify.Cooldown");
    this.duration = Spirits.plugin.getConfig().getLong("Abilities.Spirits.Water.Purify.Duration");
    this.range = Spirits.plugin.getConfig().getDouble("Abilities.Spirits.Water.Purify.Range");
    this.setElement = Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Water.Purify.SetElement");
  }

  public double calculateSize(LivingEntity entity) {
    return (entity.getEyeLocation().distance(entity.getLocation()) / 2 + 0.8D);
  }

  @Override
  public void remove() {
    super.remove();

    if (target != null) {
      heldEntities.remove(target.getEntityId());
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
    return "Purify";
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
  public void progress() {
    if (!bPlayer.canBendIgnoreCooldowns(this)) {
      remove();
      return;
    }

    if (target == null || target.isDead()) {
      remove();
      return;
    }

    if (!target.getWorld().equals(player.getWorld())) {
      remove();
      return;
    }

    if (target.getLocation().distance(player.getLocation()) > 25) {

      remove();
      return;
    }

    if (System.currentTimeMillis() - time > 10000L) {
      MovementHandler mh = new MovementHandler((Player) player, this);
      mh.stopWithDuration(5, ChatColor.YELLOW + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "* READY *");
      charged = true;
      createNewSpirals();
    }

    if (System.currentTimeMillis() - time > duration) {
      remove();
      bPlayer.addCooldown(this);
      return;
    }

    if (charged) {
      if (target instanceof Player && setElement) {
        BendingPlayer bPlayer = BendingPlayer.getBendingPlayer((Player) target);
        if (bPlayer.hasElement(SpiritElement.DARK_SPIRIT)) {
          bPlayer.addElement(SpiritElement.LIGHT_SPIRIT);
          bPlayer.getElements().remove(SpiritElement.DARK_SPIRIT);
          GeneralMethods.saveElements(bPlayer);
          player.sendMessage(Element.WATER.getColor() + "You have successfully purified the " + SpiritElement.DARK_SPIRIT.getColor() + "Dark Spirit.");
          target.sendMessage(SpiritElement.LIGHT_SPIRIT.getColor() + "You are now a" + ChatColor.BOLD + "" + ChatColor.AQUA + " LightSpirit");
          ParticleEffect.FIREWORKS_SPARK.display(target.getLocation(), 3, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F);
        } else {
          target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 2));
          target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 2));
          target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
          ParticleEffect.FIREWORKS_SPARK.display(target.getLocation(), 3, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F);
        }
      } else if (target instanceof Entity || target instanceof LivingEntity) {
        target.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 300, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 300, 2));
        target.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 2));
        ParticleEffect.FIREWORKS_SPARK.display(target.getLocation(), 3, (float) Math.random(), (float) Math.random(), (float) Math.random(), 0.0F);
      }
    }

    if (stage == 0) {

      if (!player.isSneaking()) {
        bPlayer.addCooldown(this);
        remove();
        return;
      }

      if (travelLoc == null && this.getStartTime() + duration < System.currentTimeMillis()) {
        remove();
        bPlayer.addCooldown(this);
        travelLoc = player.getEyeLocation();
        return;
      } else if (travelLoc == null) {
        ticks++;
        Long chargingTime = System.currentTimeMillis() - getStartTime();
        this.chargeTicks = (int) (chargingTime / 25);
        if (!charged) {
          createSpirals();
        } else {
          createNewSpirals();
        }
        //ParticleEffect.MAGIC_CRIT.display(0.3F, 0.3F, 0.3F, 0.1F, 8, target.getLocation().clone().add(0, 0.8, 0), 90);
        //f7f2f6
        for (int i = -180; i < 180; i += 10) {
          target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 300, 128));
          target.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 300, 128));
        }
        return;
      }
    }
  }

  public void paralyze(Entity entity) {
    if (entity instanceof Creature) {
      ((Creature) entity).setTarget(null);
    }

    if (entity instanceof Player) {
      if (Suffocate.isChannelingSphere((Player) entity)) {
        Suffocate.remove((Player) entity);
      }
    }
    MovementHandler mh = new MovementHandler((LivingEntity) entity, this);
    mh.stop(ChatColor.YELLOW + "* PURIFYING *");
  }

  private void createSpirals() {
    if (hasReached) {
      int amount = chargeTicks + 2;
      double maxHeight = 4;
      double distanceFromPlayer = 1.5;

      int angle = 5 * amount + 5 * ticks;
      double x = Math.cos(Math.toRadians(angle)) * distanceFromPlayer;
      double z = Math.sin(Math.toRadians(angle)) * distanceFromPlayer;
      double height = (amount * 0.10) % maxHeight;
      Location displayLoc = target.getLocation().clone().add(x, height, z);

      int angle2 = 5 * amount + 180 + 5 * ticks;
      double x2 = Math.cos(Math.toRadians(angle2)) * distanceFromPlayer;
      double z2 = Math.sin(Math.toRadians(angle2)) * distanceFromPlayer;
      Location displayLoc2 = target.getLocation().clone().add(x2, height, z2);
      GeneralMethods.displayColoredParticle("42aaf4", displayLoc2);
      GeneralMethods.displayColoredParticle("42aaf4", displayLoc);
      GeneralMethods.displayColoredParticle("70ddff", displayLoc2);
      GeneralMethods.displayColoredParticle("70ddff", displayLoc);
//      ParticleEffect.SPELL_MOB.display(displayLoc2, 1);
//      ParticleEffect.SPELL_MOB.display(displayLoc, 1);
    }
  }

  private void createNewSpirals() {
    if (hasReached) {
      int amount = chargeTicks + 2;
      double maxHeight = 4;
      double distanceFromPlayer = 1.5;

      int angle = 5 * amount + 5 * ticks;
      double x = Math.cos(Math.toRadians(angle)) * distanceFromPlayer;
      double z = Math.sin(Math.toRadians(angle)) * distanceFromPlayer;
      double height = (amount * 0.10) % maxHeight;
      Location displayLoc = target.getLocation().clone().add(x, height, z);

      int angle2 = 5 * amount + 180 + 5 * ticks;
      double x2 = Math.cos(Math.toRadians(angle2)) * distanceFromPlayer;
      double z2 = Math.sin(Math.toRadians(angle2)) * distanceFromPlayer;
      Location displayLoc2 = target.getLocation().clone().add(x2, height, z2);
      GeneralMethods.displayColoredParticle("faff9b", displayLoc2);
      GeneralMethods.displayColoredParticle("faff9b", displayLoc);
      GeneralMethods.displayColoredParticle("f6ff5e", displayLoc2);
      GeneralMethods.displayColoredParticle("f6ff5e", displayLoc);
    }
  }

  @Override
  public String getAuthor() {
    return "Prride & Svetroid";
  }

  @Override
  public String getDescription() {
    return Spirits.plugin.getConfig().getString("Language.Abilities.Water.Purify.Description");
  }

  @Override
  public String getInstructions() {
    return Spirits.plugin.getConfig().getString("Language.Abilities.Water.Purify.Instructions");
  }

  public String getVersion() {
    return Methods.getVersion();
  }

  @Override
  public boolean isEnabled() {
    return Spirits.plugin.getConfig().getBoolean("Abilities.Spirits.Water.Purify.Enabled");
  }

  @Override
  public void load() {
  }

  @Override
  public void stop() {
  }

}
