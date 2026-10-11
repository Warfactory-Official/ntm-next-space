// SPDX-FileCopyrightText: 2026 movblock <admin@movblock.mov>
// SPDX-License-Identifier: LGPL-3.0-only

package com.hbmspace.blocks.fluid;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

final class SchrabidicLegacyMaterials {

    private static final Set<Block> IRON =
            blocks(
                    """
            hbm:ancient_scrap hbm:anvil_arsenic_bronze hbm:anvil_bismuth_bronze hbm:anvil_desh hbm:anvil_dnt
            hbm:anvil_ferrouranium hbm:anvil_iron hbm:anvil_lead hbm:anvil_murky hbm:anvil_osmiridium
            hbm:anvil_saturnite hbm:anvil_schrabidate hbm:anvil_steel hbm:barbed_wire hbm:barbed_wire_acid
            hbm:barbed_wire_fire hbm:barbed_wire_poison hbm:barbed_wire_ultradeath hbm:barbed_wire_wither hbm:barrel_antimatter
            hbm:barrel_corroded hbm:barrel_plastic hbm:barrel_steel hbm:barrel_tcalloy hbm:blast_door
            hbm:block_aluminium hbm:block_australium hbm:block_beryllium hbm:block_bismuth hbm:block_boron
            hbm:block_cadmium hbm:block_cap_fritz hbm:block_cap_korl hbm:block_cap_nuka hbm:block_cap_quantum
            hbm:block_cap_rad hbm:block_cap_sparkle hbm:block_cdalloy hbm:block_cobalt hbm:block_coke_coal
            hbm:block_coke_lignite hbm:block_coke_petroleum hbm:block_coltan hbm:block_combine_steel hbm:block_corium
            hbm:block_corium_cobble hbm:block_desh hbm:block_dineutronium hbm:block_dura_steel hbm:block_electrical_scrap
            hbm:block_euphemium hbm:block_fluorite hbm:block_graphite hbm:block_lanthanium hbm:block_lead
            hbm:block_lithium hbm:block_magnetized_tungsten hbm:block_niobium hbm:block_niter hbm:block_red_copper
            hbm:block_starmetal hbm:block_steel hbm:block_sulfur hbm:block_tantalium hbm:block_tcalloy
            hbm:block_titanium hbm:block_tungsten hbm:block_zirconium hbm:boat hbm:bobblehead
            hbm:bomb_multi hbm:book_guide hbm:boxcar hbm:broadcaster_pc hbm:cable
            hbm:cable_detector hbm:cable_diode hbm:cable_switch hbm:capacitor_copper hbm:cargo_door
            hbm:cargo_elevator hbm:charger hbm:chimney_brick hbm:chimney_industrial hbm:cm_block_alloy
            hbm:cm_block_desh hbm:cm_block_steel hbm:cm_block_tcalloy hbm:cm_circuit_aluminium hbm:cm_circuit_copper
            hbm:cm_circuit_gold hbm:cm_circuit_red_copper hbm:cm_circuit_schrabidium hbm:cm_engine_bismuth hbm:cm_engine_desh
            hbm:cm_engine_standard hbm:cm_flux hbm:cm_heat hbm:cm_port_alloy hbm:cm_port_desh
            hbm:cm_port_steel hbm:cm_port_tcalloy hbm:cm_sheet_alloy hbm:cm_sheet_desh hbm:cm_sheet_steel
            hbm:cm_sheet_tcalloy hbm:cm_tank_alloy hbm:cm_tank_desh hbm:cm_tank_steel hbm:cm_tank_tcalloy
            hbm:compact_launcher hbm:conveyor hbm:conveyor_chute hbm:conveyor_double hbm:conveyor_express
            hbm:conveyor_lift hbm:conveyor_triple hbm:crane_boxer hbm:crane_extractor hbm:crane_grabber
            hbm:crane_inserter hbm:crane_partitioner hbm:crane_router hbm:crane_splitter hbm:crane_unboxer
            hbm:crashed_balefire hbm:crashed_conventional hbm:crashed_nuke hbm:crashed_salted hbm:crate_ammo
            hbm:crate_desh hbm:crate_iron hbm:crate_lead hbm:crate_metal hbm:crate_red
            hbm:crate_steel hbm:crate_tungsten hbm:crystal_hardened hbm:crystal_pulsar hbm:crystal_virus
            hbm:custom_machine hbm:custom_machine_anchor hbm:deco_aluminium hbm:deco_beryllium hbm:deco_computer
            hbm:deco_crt_blinking hbm:deco_crt_broken hbm:deco_crt_bsod hbm:deco_crt_clean hbm:deco_emitter
            hbm:deco_lead hbm:deco_loot hbm:deco_pipe hbm:deco_pipe_framed hbm:deco_pipe_framed_green
            hbm:deco_pipe_framed_green_rusted hbm:deco_pipe_framed_marked hbm:deco_pipe_framed_red hbm:deco_pipe_framed_rusted hbm:deco_pipe_green
            hbm:deco_pipe_green_rusted hbm:deco_pipe_marked hbm:deco_pipe_quad hbm:deco_pipe_quad_green hbm:deco_pipe_quad_green_rusted
            hbm:deco_pipe_quad_marked hbm:deco_pipe_quad_red hbm:deco_pipe_quad_rusted hbm:deco_pipe_red hbm:deco_pipe_rim
            hbm:deco_pipe_rim_green hbm:deco_pipe_rim_green_rusted hbm:deco_pipe_rim_marked hbm:deco_pipe_rim_red hbm:deco_pipe_rim_rusted
            hbm:deco_pipe_rusted hbm:deco_rbmk hbm:deco_rbmk_smooth hbm:deco_red_copper hbm:deco_rusty_steel
            hbm:deco_steel hbm:deco_titanium hbm:deco_toaster_iron hbm:deco_toaster_steel hbm:deco_toaster_wood
            hbm:deco_tungsten hbm:decon hbm:det_charge hbm:det_cord hbm:det_miner
            hbm:det_nuke hbm:dfc_core hbm:dfc_emitter hbm:dfc_injector hbm:dfc_receiver
            hbm:dfc_stabilizer hbm:door_bunker hbm:door_metal hbm:door_office hbm:door_red
            hbm:drone_crate hbm:drone_crate_provider hbm:drone_crate_requester hbm:drone_dock hbm:dungeon_chain
            hbm:emp_bomb hbm:fan hbm:fence_metal hbm:fence_metal_post hbm:field_disturber
            hbm:filing_cabinet_green hbm:filing_cabinet_steel hbm:fire_door hbm:fireworks hbm:flame_war
            hbm:float_bomb hbm:floodlight hbm:fluid_counter_valve hbm:fluid_duct_box_copper_0 hbm:fluid_duct_box_copper_1
            hbm:fluid_duct_box_copper_2 hbm:fluid_duct_box_copper_3 hbm:fluid_duct_box_copper_4 hbm:fluid_duct_box_silver_0 hbm:fluid_duct_box_silver_1
            hbm:fluid_duct_box_silver_2 hbm:fluid_duct_box_silver_3 hbm:fluid_duct_box_silver_4 hbm:fluid_duct_box_white_0 hbm:fluid_duct_box_white_1
            hbm:fluid_duct_box_white_2 hbm:fluid_duct_box_white_3 hbm:fluid_duct_box_white_4 hbm:fluid_duct_exhaust_0 hbm:fluid_duct_exhaust_1
            hbm:fluid_duct_exhaust_2 hbm:fluid_duct_exhaust_3 hbm:fluid_duct_exhaust_4 hbm:fluid_duct_gauge hbm:fluid_duct_paintable
            hbm:fluid_duct_paintable_block_exhaust hbm:fluid_pipe hbm:fluid_pipe_colored hbm:fluid_pipe_silver hbm:fluid_pump
            hbm:fluid_switch hbm:fluid_valve hbm:fraction_spacer hbm:furnace_iron hbm:furnace_steel
            hbm:fusion_boiler hbm:fusion_breeder hbm:fusion_collector hbm:fusion_component hbm:fusion_component_blanket
            hbm:fusion_component_bscco_welded hbm:fusion_component_motor hbm:fusion_coupler hbm:fusion_hatch hbm:fusion_heater
            hbm:fusion_klystron hbm:fusion_klystron_creative hbm:fusion_mhdt hbm:fusion_plasma_forge hbm:fusion_torus
            hbm:geiger hbm:gravel_obsidian hbm:hadron_coil_alloy hbm:heater_electric hbm:heater_firebox
            hbm:heater_heatex hbm:heater_oilburner hbm:hev_battery hbm:icf hbm:icf_block
            hbm:icf_capacitor hbm:icf_casing hbm:icf_cell hbm:icf_component hbm:icf_component_structure
            hbm:icf_component_structure_bolted hbm:icf_component_vessel hbm:icf_component_vessel_welded hbm:icf_controller hbm:icf_emitter
            hbm:icf_port hbm:icf_turbocharger hbm:lamp_demon hbm:lantern hbm:lantern_behemoth
            hbm:large_vehicle_door hbm:launch_pad hbm:launch_pad_large hbm:launch_pad_rusted hbm:launch_table
            hbm:launchpad_soyuz hbm:lox_barrel hbm:machine_ammo_press hbm:machine_annihilator hbm:machine_arc_furnace
            hbm:machine_arc_welder hbm:machine_armor_table hbm:machine_ashpit hbm:machine_assembly_factory hbm:machine_assembly_machine
            hbm:machine_autocrafter hbm:machine_autosaw hbm:machine_battery hbm:machine_battery_redd hbm:machine_battery_socket
            hbm:machine_bigasstank hbm:machine_boiler hbm:machine_boiler_off hbm:machine_catalytic_reformer hbm:machine_centrifuge
            hbm:machine_chemical_factory hbm:machine_chemical_plant hbm:machine_chungus hbm:machine_coker hbm:machine_combustion_engine
            hbm:machine_compressor hbm:machine_compressor_compact hbm:machine_condenser hbm:machine_condenser_powered hbm:machine_controller
            hbm:machine_conveyor_press hbm:machine_cracking_tower hbm:machine_crystallizer hbm:machine_cyclotron hbm:machine_detector
            hbm:machine_deuterium_extractor hbm:machine_deuterium_tower hbm:machine_diesel hbm:machine_drain hbm:machine_electric_furnace
            hbm:machine_electrolyser hbm:machine_epress hbm:machine_excavator hbm:machine_exposure_chamber hbm:machine_fel
            hbm:machine_fensu hbm:machine_flare hbm:machine_fluidtank hbm:machine_forcefield hbm:machine_fracking_tower
            hbm:machine_fraction_tower hbm:machine_funnel hbm:machine_furnace_brick hbm:machine_gascent hbm:machine_hephaestus
            hbm:machine_hydrotreater hbm:machine_icf_press hbm:machine_industrial_boiler hbm:machine_industrial_turbine hbm:machine_intake
            hbm:machine_keyforge hbm:machine_large_turbine hbm:machine_liquefactor hbm:machine_lpw2 hbm:machine_microwave
            hbm:machine_mining_laser hbm:machine_missile_assembly hbm:machine_mixer hbm:machine_orbus hbm:machine_ore_slopper
            hbm:machine_precass hbm:machine_press hbm:machine_puf6_tank hbm:machine_pumpjack hbm:machine_purex
            hbm:machine_pyrooven hbm:machine_radar hbm:machine_radar_large hbm:machine_radgen hbm:machine_radiolysis
            hbm:machine_reactor hbm:machine_reactor_small hbm:machine_refinery hbm:machine_rockmill hbm:machine_rotary_furnace
            hbm:machine_rtg_grey hbm:machine_satlink hbm:machine_satlinker hbm:machine_sawmill hbm:machine_shredder
            hbm:machine_silex hbm:machine_siren hbm:machine_solar_boiler hbm:machine_soldering_station hbm:machine_solidifier
            hbm:machine_steam_engine hbm:machine_stirling hbm:machine_stirling_creative hbm:machine_stirling_steel hbm:machine_storage_drum
            hbm:machine_strand_caster hbm:machine_supercomputer hbm:machine_tape_drive hbm:machine_teleporter hbm:machine_thresher
            hbm:machine_tower_large hbm:machine_tower_small hbm:machine_transformer hbm:machine_turbine hbm:machine_turbinegas
            hbm:machine_turbofan hbm:machine_uf6_tank hbm:machine_vacuum_distill hbm:machine_waste_drum hbm:machine_weapon_table
            hbm:machine_well hbm:machine_wood_burner hbm:mass_storage_desh hbm:mass_storage_iron hbm:mass_storage_tcalloy
            hbm:mass_storage_wood hbm:mine_ap hbm:mine_fat hbm:mine_he hbm:mine_naval
            hbm:mine_shrap hbm:nuke_boy hbm:nuke_custom hbm:nuke_fleija hbm:nuke_fstbmb
            hbm:nuke_gadget hbm:nuke_man hbm:nuke_mike hbm:nuke_n2 hbm:nuke_prototype
            hbm:nuke_solinium hbm:nuke_tsar hbm:obj_tester hbm:oil_pipe hbm:pa_beamline
            hbm:pa_detector hbm:pa_dipole hbm:pa_quadrupole hbm:pa_rfc hbm:pa_source
            hbm:part_emitter hbm:pile_block hbm:pile_control hbm:pile_loader hbm:pile_vent
            hbm:pink_barrel hbm:pipe_anchor hbm:piston_inserter hbm:platemetal hbm:pneumatic_storage_access
            hbm:pneumatic_storage_clutter hbm:pneumatic_storage_exporter hbm:pneumatic_storage_importer hbm:pneumatic_storage_mono hbm:pneumatic_tube
            hbm:pneumatic_tube_paintable hbm:pole_satellite_receiver hbm:pole_top hbm:press_preheater hbm:pump_electric
            hbm:pump_steam hbm:pwr_block hbm:pwr_casing hbm:pwr_channel hbm:pwr_control
            hbm:pwr_controller hbm:pwr_fuel hbm:pwr_heatex hbm:pwr_heatsink hbm:pwr_neutron_source
            hbm:pwr_port hbm:pwr_reflector hbm:qe_containment hbm:qe_sliding_door hbm:rad_absorber
            hbm:rad_absorber_green hbm:rad_absorber_pink hbm:rad_absorber_red hbm:radar_screen hbm:radio_autocal
            hbm:radiobox hbm:radiorec hbm:rail_large_buffer hbm:rail_large_curve hbm:rail_large_curve_7
            hbm:rail_large_curve_9 hbm:rail_large_ramp hbm:rail_large_straight hbm:rail_large_straight_short hbm:rail_large_switch
            hbm:rail_large_switch_flipped hbm:rail_narrow_curve hbm:rail_narrow_straight hbm:rbmk_absorber hbm:rbmk_autoloader
            hbm:rbmk_blank hbm:rbmk_boiler hbm:rbmk_console hbm:rbmk_control hbm:rbmk_control_auto
            hbm:rbmk_control_mod hbm:rbmk_control_reasim hbm:rbmk_control_reasim_auto hbm:rbmk_cooler hbm:rbmk_crane_console
            hbm:rbmk_debris hbm:rbmk_debris_burning hbm:rbmk_debris_digamma hbm:rbmk_debris_radiating hbm:rbmk_display
            hbm:rbmk_display_blank hbm:rbmk_fuel_rod hbm:rbmk_fuel_rod_moderated hbm:rbmk_fuel_rod_reasim hbm:rbmk_fuel_rod_reasim_moderated
            hbm:rbmk_gauge hbm:rbmk_graph hbm:rbmk_heater hbm:rbmk_indicator hbm:rbmk_key_pad
            hbm:rbmk_lever hbm:rbmk_loader hbm:rbmk_moderator hbm:rbmk_numitron hbm:rbmk_outgasser
            hbm:rbmk_reflector hbm:rbmk_steam_inlet hbm:rbmk_steam_outlet hbm:rbmk_storage hbm:rbmk_terminal
            hbm:reactor_zirnox hbm:rebar hbm:red_barrel hbm:red_cable_box_0 hbm:red_cable_box_1
            hbm:red_cable_box_2 hbm:red_cable_box_3 hbm:red_cable_box_4 hbm:red_cable_classic hbm:red_cable_gauge
            hbm:red_cable_paintable hbm:red_connector hbm:red_connector_super hbm:red_pylon hbm:red_pylon_large
            hbm:red_pylon_medium_steel hbm:red_pylon_medium_steel_transformer hbm:red_pylon_steel hbm:red_wire_coated hbm:refueler
            hbm:round_airlock_door hbm:safe hbm:sat_dock hbm:seal_controller hbm:seal_frame
            hbm:seal_hatch hbm:secure_access_door hbm:silo_hatch hbm:silo_hatch_large hbm:slag
            hbm:sliding_blast_door hbm:sliding_seal_door hbm:solar_mirror hbm:soyuz_capsule hbm:soyuz_launcher
            hbm:spikes hbm:spotlight_fluoro hbm:spotlight_halogen hbm:spotlight_incandescent hbm:statue_elb_f
            hbm:steel_beam hbm:steel_corner hbm:steel_grate hbm:steel_grate_wide hbm:steel_poles
            hbm:steel_roof hbm:steel_scaffold hbm:steel_scaffold_red hbm:steel_scaffold_white hbm:steel_scaffold_yellow
            hbm:steel_wall hbm:struct_icf_core hbm:struct_launcher hbm:struct_launcher_core hbm:struct_launcher_core_large
            hbm:struct_scaffold hbm:struct_soyuz_core hbm:struct_torus_core hbm:struct_watz_core hbm:substation
            hbm:taint hbm:taint_barrel hbm:tape_recorder hbm:teleanchor hbm:tesla
            hbm:therm_endo hbm:therm_exo hbm:transition_seal hbm:trapdoor_steel hbm:turret_arty
            hbm:turret_chekhov hbm:turret_friendly hbm:turret_fritz hbm:turret_himars hbm:turret_howard
            hbm:turret_howard_damaged hbm:turret_jeremy hbm:turret_maxwell hbm:turret_richard hbm:turret_sentry
            hbm:turret_sentry_damaged hbm:turret_tauon hbm:vault_door hbm:vending_machine hbm:vending_machine_snacks
            hbm:vent_chlorine hbm:vent_chlorine_seal hbm:vent_cloud hbm:vent_pink_cloud hbm:vitrified_barrel
            hbm:volcano_core hbm:volcano_rad_core hbm:water_door hbm:watz hbm:watz_cooler
            hbm:watz_element hbm:watz_end hbm:watz_end_bolted hbm:watz_pump hbm:yellow_barrel
            hbm:zirnox_destroyed hbmspace:air_scrubber hbmspace:air_vent hbmspace:algae_film hbmspace:atmosphere_editor
            hbmspace:block_nickel hbmspace:block_osmiridium hbmspace:capacitor_complex hbmspace:combat_drop hbmspace:deco_stainless
            hbmspace:det_salt hbmspace:dish_control hbmspace:dyson_converter_anatmogenesis hbmspace:dyson_converter_he hbmspace:dyson_converter_tu
            hbmspace:dyson_launcher hbmspace:dyson_receiver hbmspace:fence_gate hbmspace:gas_dock hbmspace:hydrobay
            hbmspace:launch_pad_rocket hbmspace:machine_alkylation hbmspace:machine_atmo_emitter hbmspace:machine_atmo_tower hbmspace:machine_atmo_vent
            hbmspace:machine_cryo_distill hbmspace:machine_drive_processor hbmspace:machine_htr3 hbmspace:machine_htrf4 hbmspace:machine_htrf4neo
            hbmspace:machine_magma hbmspace:machine_milk_reformer hbmspace:machine_radiator hbmspace:machine_rocket_assembly hbmspace:machine_solar
            hbmspace:machine_stardar hbmspace:machine_vacuum_circuit hbmspace:machine_xenon_thruster hbmspace:nuke_antimatter hbmspace:oil_barrel
            hbmspace:orbital_station hbmspace:orbital_station_computer hbmspace:orbital_station_launcher hbmspace:orbital_station_port hbmspace:orrery
            hbmspace:propulsion_creative hbmspace:transporter_rocket hbmspace:troll_disturber hbmspace:war_controller minecraft:brewing_stand
            minecraft:cauldron minecraft:chain_command_block minecraft:chiseled_copper minecraft:command_block minecraft:copper_bars
            minecraft:copper_block minecraft:copper_bulb minecraft:copper_chain minecraft:copper_chest minecraft:copper_door
            minecraft:copper_golem_statue minecraft:copper_grate minecraft:copper_lantern minecraft:copper_trapdoor minecraft:cut_copper
            minecraft:cut_copper_slab minecraft:cut_copper_stairs minecraft:diamond_block minecraft:emerald_block minecraft:exposed_chiseled_copper
            minecraft:exposed_copper minecraft:exposed_copper_bars minecraft:exposed_copper_bulb minecraft:exposed_copper_chain minecraft:exposed_copper_chest
            minecraft:exposed_copper_door minecraft:exposed_copper_golem_statue minecraft:exposed_copper_grate minecraft:exposed_copper_lantern minecraft:exposed_copper_trapdoor
            minecraft:exposed_cut_copper minecraft:exposed_cut_copper_slab minecraft:exposed_cut_copper_stairs minecraft:exposed_lightning_rod minecraft:gold_block
            minecraft:heavy_weighted_pressure_plate minecraft:hopper minecraft:iron_bars minecraft:iron_block minecraft:iron_chain
            minecraft:iron_door minecraft:iron_trapdoor minecraft:lantern minecraft:lapis_block minecraft:lava_cauldron
            minecraft:light_weighted_pressure_plate minecraft:lightning_rod minecraft:oxidized_chiseled_copper minecraft:oxidized_copper minecraft:oxidized_copper_bars
            minecraft:oxidized_copper_bulb minecraft:oxidized_copper_chain minecraft:oxidized_copper_chest minecraft:oxidized_copper_door minecraft:oxidized_copper_golem_statue
            minecraft:oxidized_copper_grate minecraft:oxidized_copper_lantern minecraft:oxidized_copper_trapdoor minecraft:oxidized_cut_copper minecraft:oxidized_cut_copper_slab
            minecraft:oxidized_cut_copper_stairs minecraft:oxidized_lightning_rod minecraft:powder_snow_cauldron minecraft:raw_copper_block minecraft:raw_gold_block
            minecraft:raw_iron_block minecraft:redstone_block minecraft:repeating_command_block minecraft:soul_lantern minecraft:water_cauldron
            minecraft:waxed_chiseled_copper minecraft:waxed_copper_bars minecraft:waxed_copper_block minecraft:waxed_copper_bulb minecraft:waxed_copper_chain
            minecraft:waxed_copper_chest minecraft:waxed_copper_door minecraft:waxed_copper_golem_statue minecraft:waxed_copper_grate minecraft:waxed_copper_lantern
            minecraft:waxed_copper_trapdoor minecraft:waxed_cut_copper minecraft:waxed_cut_copper_slab minecraft:waxed_cut_copper_stairs minecraft:waxed_exposed_chiseled_copper
            minecraft:waxed_exposed_copper minecraft:waxed_exposed_copper_bars minecraft:waxed_exposed_copper_bulb minecraft:waxed_exposed_copper_chain minecraft:waxed_exposed_copper_chest
            minecraft:waxed_exposed_copper_door minecraft:waxed_exposed_copper_golem_statue minecraft:waxed_exposed_copper_grate minecraft:waxed_exposed_copper_lantern minecraft:waxed_exposed_copper_trapdoor
            minecraft:waxed_exposed_cut_copper minecraft:waxed_exposed_cut_copper_slab minecraft:waxed_exposed_cut_copper_stairs minecraft:waxed_exposed_lightning_rod minecraft:waxed_lightning_rod
            minecraft:waxed_oxidized_chiseled_copper minecraft:waxed_oxidized_copper minecraft:waxed_oxidized_copper_bars minecraft:waxed_oxidized_copper_bulb minecraft:waxed_oxidized_copper_chain
            minecraft:waxed_oxidized_copper_chest minecraft:waxed_oxidized_copper_door minecraft:waxed_oxidized_copper_golem_statue minecraft:waxed_oxidized_copper_grate minecraft:waxed_oxidized_copper_lantern
            minecraft:waxed_oxidized_copper_trapdoor minecraft:waxed_oxidized_cut_copper minecraft:waxed_oxidized_cut_copper_slab minecraft:waxed_oxidized_cut_copper_stairs minecraft:waxed_oxidized_lightning_rod
            minecraft:waxed_weathered_chiseled_copper minecraft:waxed_weathered_copper minecraft:waxed_weathered_copper_bars minecraft:waxed_weathered_copper_bulb minecraft:waxed_weathered_copper_chain
            minecraft:waxed_weathered_copper_chest minecraft:waxed_weathered_copper_door minecraft:waxed_weathered_copper_golem_statue minecraft:waxed_weathered_copper_grate minecraft:waxed_weathered_copper_lantern
            minecraft:waxed_weathered_copper_trapdoor minecraft:waxed_weathered_cut_copper minecraft:waxed_weathered_cut_copper_slab minecraft:waxed_weathered_cut_copper_stairs minecraft:waxed_weathered_lightning_rod
            minecraft:weathered_chiseled_copper minecraft:weathered_copper minecraft:weathered_copper_bars minecraft:weathered_copper_bulb minecraft:weathered_copper_chain
            minecraft:weathered_copper_chest minecraft:weathered_copper_door minecraft:weathered_copper_golem_statue minecraft:weathered_copper_grate minecraft:weathered_copper_lantern
            minecraft:weathered_copper_trapdoor minecraft:weathered_cut_copper minecraft:weathered_cut_copper_slab minecraft:weathered_cut_copper_stairs minecraft:weathered_lightning_rod
            """);
    private static final Set<Block> GLASS =
            blocks(
                    """
            hbm:block_tritium hbm:glass_ash hbm:glass_boron hbm:glass_lead hbm:glass_polarized
            hbm:glass_polonium hbm:glass_trinitite hbm:glass_uranium hbm:snowglobe minecraft:beacon
            minecraft:black_stained_glass minecraft:black_stained_glass_pane minecraft:blue_stained_glass minecraft:blue_stained_glass_pane minecraft:brown_stained_glass
            minecraft:brown_stained_glass_pane minecraft:cyan_stained_glass minecraft:cyan_stained_glass_pane minecraft:glass minecraft:glass_pane
            minecraft:glowstone minecraft:gray_stained_glass minecraft:gray_stained_glass_pane minecraft:green_stained_glass minecraft:green_stained_glass_pane
            minecraft:light_blue_stained_glass minecraft:light_blue_stained_glass_pane minecraft:light_gray_stained_glass minecraft:light_gray_stained_glass_pane minecraft:lime_stained_glass
            minecraft:lime_stained_glass_pane minecraft:magenta_stained_glass minecraft:magenta_stained_glass_pane minecraft:orange_stained_glass minecraft:orange_stained_glass_pane
            minecraft:pink_stained_glass minecraft:pink_stained_glass_pane minecraft:purple_stained_glass minecraft:purple_stained_glass_pane minecraft:red_stained_glass
            minecraft:red_stained_glass_pane minecraft:white_stained_glass minecraft:white_stained_glass_pane minecraft:yellow_stained_glass minecraft:yellow_stained_glass_pane
            """);

    private static final Set<Block> WATER =
            blocks(
                    """
            hbm:schrabidic_block hbm:sulfuric_acid_block hbmspace:bromine_block hbmspace:ccl_block hbmspace:laythe_coral_brain
            hbmspace:laythe_coral_bubble hbmspace:laythe_coral_fire hbmspace:laythe_coral_horn hbmspace:laythe_coral_tube hbmspace:laythe_glowgrass
            hbmspace:laythe_kelp hbmspace:laythe_seagrass hbmspace:mercury_block hbmspace:plant_tall_laythe minecraft:water
            """);

    private SchrabidicLegacyMaterials() {}

    static boolean iron(Block block) {
        return IRON.contains(block);
    }

    static boolean glass(Block block) {
        return GLASS.contains(block);
    }

    static boolean water(Block block) {
        return WATER.contains(block);
    }

    private static Set<Block> blocks(String ids) {
        Set<Block> blocks = Collections.newSetFromMap(new IdentityHashMap<>());
        for (String id : ids.split("\\s+")) {
            if (id.isEmpty()) continue;
            Block block = BuiltInRegistries.BLOCK.getOptional(Identifier.parse(id)).orElseThrow();
            blocks.add(block);
        }
        return blocks;
    }
}
