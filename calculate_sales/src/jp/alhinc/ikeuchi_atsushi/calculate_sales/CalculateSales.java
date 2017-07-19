package jp.alhinc.ikeuchi_atsushi.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	public static void main(String[] args) {
		HashMap<String, String> mapBranch = new HashMap<String, String>();
		HashMap<String, String> mapCommodity = new HashMap<String, String>();
		HashMap<String, Long> mapSale = new HashMap<String, Long>();
		HashMap<String, Long> mapCommodityAggregate = new HashMap<String, Long>();
		BufferedReader br = null;
		BufferedWriter bw = null;

		if(args.length != 1){
			System.out.println("予期せぬエラーが発生しました");
			return;
		}
		try {
			File file = new File(args[0], "branch.lst");
			if (!file.exists()){
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String s;
			while ((s = br.readLine()) != null) {
				String[] branches = s.split(",");

				if (!branches[0].matches("\\d{3}")||branches.length != 2) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				mapBranch.put(branches[0], branches[1]);
				mapSale.put(branches[0], 0L);
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
		}
		try {
			File file = new File(args[0], "commodity.lst");
			if (!file.exists()){
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String st;
			while ((st = br.readLine()) != null) {
				String[] commodities = st.split(",");

				if (!commodities[0].matches("\\w{8}")||commodities.length != 2) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				mapCommodity.put(commodities[0], commodities[1]);
				mapCommodityAggregate.put(commodities[0], 0L);
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		File file = new File(args[0]);
		File files[] = file.listFiles();
		ArrayList<File> pass = new ArrayList<File>();

		for (int i = 0; i < files.length; i++) {
			String fileName = files[i].getName();
			if (fileName.matches("\\d{8}.rcd$")&&(files[i].isFile())){
				pass.add(files[i]);
			}
		}
		ArrayList<Integer> numbersList = new ArrayList<Integer>();

		for(int i = 0; i < pass.size() ; i++){
		String[] number = pass.get(i).getName().split("\\.");
		int numbers = Integer.parseInt(number[0]);
		numbersList.add(numbers);
		}
		for(int i = 0; i < numbersList.size() - 1; i++){
			int plus = numbersList.get(i + 1) - numbersList.get(i);
			if(plus != 1){
				System.out.println("売上ファイル名が連番になっていません");
				return;
			}
		}
		try {
			for (int i = 0; i < pass.size(); i++) {
				if (!pass.get(i).exists()){
					System.out.println("rcdファイルが存在しません");
				}
				br = new BufferedReader(new FileReader(pass.get(i)));
				String str;
				ArrayList<String> earnings = new ArrayList<String>();
				while ((str = br.readLine()) != null) {
					earnings.add(str);
				}
				if(earnings.size() != 3){
					System.out.println(pass.get(i).getName() + "のフォーマットが不正です");
					return;
				}
				if (!mapSale.containsKey(earnings.get(0))){
					System.out.println(pass.get(i).getName() +  "の支店コードが不正です");
					return;
				}
				if (!mapCommodityAggregate.containsKey(earnings.get(1))){
					System.out.println(pass.get(i).getName() +  "の商品コードが不正です");
					return;
				}
				long shop = mapSale.get(earnings.get(0));
				long com = mapCommodityAggregate.get(earnings.get(1));

				if(!earnings.get(2).matches("^[0-9]+$")){
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
				shop += Long.parseLong(earnings.get(2));
				com += Long.parseLong(earnings.get(2));

				mapSale.put(earnings.get(0),shop);
				mapCommodityAggregate.put(earnings.get(1),com);

				if(!String.valueOf(shop).matches("\\d{1,10}") || !String.valueOf(com).matches("\\d{1,10}")){
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}
		List<Map.Entry<String,Long>> entriesShopAgg = new ArrayList<Map.Entry<String,Long>>(mapSale.entrySet());
		Collections.sort(entriesShopAgg, new Comparator<Map.Entry<String,Long>>() {
			@Override
			public int compare(
			Entry<String,Long> entry1, Entry<String,Long> entry2) {
				return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
			}
		});
			try{
				File shopAgg = new File(args[0],"branch.out");
				bw = new BufferedWriter(new FileWriter(shopAgg));
				for (Entry<String,Long> s : entriesShopAgg) {
					bw.write(s.getKey() + "," + mapBranch.get(s.getKey()) + "," + (s.getValue()));
					bw.newLine();
				}
				bw.close();
			}catch(IOException e){
				System.out.println("予期せぬエラーが発生しました");
				return;
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						System.out.println("予期せぬエラーが発生しました");
						return;
					}
				}
			}
			List<Map.Entry<String,Long>> entriesComAgg = new ArrayList<Map.Entry<String,Long>>(mapCommodityAggregate.entrySet());
			Collections.sort(entriesComAgg, new Comparator<Map.Entry<String,Long>>() {
				@Override
				public int compare(
				Entry<String,Long> entry1, Entry<String,Long> entry2) {
					return ((Long)entry2.getValue()).compareTo((Long)entry1.getValue());
				}
			});
		try{
			File comAgg = new File(args[0],"commodity.out");
			FileWriter fwCom = new FileWriter(comAgg);
			bw = new BufferedWriter(fwCom);
			for (Entry<String,Long> s : entriesComAgg) {
				bw.write(s.getKey() + "," + mapCommodity.get(s.getKey()) + "," + (s.getValue()));
				bw.newLine();
			}
		}catch(IOException e){
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}
	}
}
