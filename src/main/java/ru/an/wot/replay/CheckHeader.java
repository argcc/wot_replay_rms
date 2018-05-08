package ru.an.wot.replay;

public class CheckHeader {
	
	public static final String[] artyllery = {
			"G02_Hummel",
			"F20_RenaultBS",
			"R15_S-51",
			"A107_T1_HMC",
			"F28_105_leFH18B2",
			"G11_Bison_I",
			"F21_Lorraine39_L_AM",
			"GB27_Sexton",
			"A16_M7_Priest",
			"R16_SU-18",
			"R27_SU-14",
			"A18_M41",
			"F22_AMX_105AM",
			"G22_Sturmpanzer_II",
			"A17_M37",
			"F23_AMX_13F3AM",
			"R14_SU-5",
			"R26_SU-8",
			"G23_Grille",
			"G19_Wespe",
			"F24_Lorraine155_50",
			"A37_M40M43",
			"F25_Lorraine155_51",
			"R66_SU-26",
			"A32_M12",
			"R51_Object_212",
			"A38_T92",
			"R52_Object_261",
			"G45_G_Tiger",
			"G49_G_Panther",
			"G61_G_E",
			"GB25_Loyd_Gun_Carriage",
			"GB26_Birch_Gun",
			"GB28_Bishop",
			"GB29_Crusader_5inch",
			"GB30_FV3805",
			"F38_Bat_Chatillon155_58",
			"GB77_FV304",
			"GB79_FV206",
			"GB31_Conqueror_Gun",
			"F67_Bat_Chatillon155_55",
			"F66_AMX_Ob_Am105",
			"G93_GW_Mk_VIe",
			"G94_GW_Tiger_P",
			"G95_Pz_Sfl_IVb",
			"R91_SU14_1",
			"A88_M53_55",
			"R100_SU122A",
			"A87_M44",
			"A108_T18_HMC",
			"A27_T82",
			"GB78_Sexton_I"
		};
	
	public static final String[] exeVersion = {
			"0.9.23.0",
			"1.0.1.0"
		}; 
	
	public void check(Replay rep) {
		
		if("epic".equals(rep.gameplayId))
			throw new ReplayException("Реплеи Линии фронта не поддерживаются");
		
		for(String arty : artyllery)
			if(rep.vehicleName.endsWith(arty))
				throw new ReplayException("Реплеи на артиллерии не поддерживаются");

		boolean vcheck = false;
		for(String exev : exeVersion)
			if(rep.clientVersionExe.endsWith(exev)) {
				vcheck = true;
				break;
			}
		
		if(!vcheck)
			throw new ReplayException(String.format("Неподдерживаемая версия реплея [exe:%s] [xml:%s]", 
					rep.clientVersionExe, rep.clientVersionXml));

	}
}
