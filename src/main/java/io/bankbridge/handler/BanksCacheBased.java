package io.bankbridge.handler;

import static io.bankbridge.handler.BanksUtil.getCommonPredicates;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.bankbridge.model.BankModel;
import io.bankbridge.model.BankModelList;
import io.bankbridge.response.BankDetails;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BanksCacheBased {


	public static CacheManager cacheManager;

	public static void init() throws Exception {
		cacheManager = CacheManagerBuilder
				.newCacheManagerBuilder().withCache("banks", CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, BankModel.class, ResourcePoolsBuilder.heap(20)))
				.build();
		cacheManager.init();
		Cache cache = cacheManager.getCache("banks", String.class, BankModel.class);
		try {
			BankModelList models = new ObjectMapper().readValue(
					Thread.currentThread().getContextClassLoader().getResource("banks-v1.json"), BankModelList.class);
			for (BankModel model : models.banks) {
				cache.put(model.bic, model);
			}
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Method for handling static Banks request
	 * @param queryMap Map holds the details of request query params.
	 * @return List of banks.
	 */
	public static List<BankDetails> handle(Map<String, String[]> queryMap) {
		List<BankModel> result = new ArrayList<>();
		cacheManager.getCache("banks", String.class, BankModel.class).forEach(entry -> {
			result.add(entry.getValue());
		});
		Map<String, String> paramsMap = BanksUtil.getParamsMap(queryMap);

		List<BankDetails> bankDetails = result.stream().map(BanksCacheBased::staticBanksMapper)
				.collect(Collectors.toList());
		if(!paramsMap.isEmpty()){
			bankDetails = filterBanksResponse(bankDetails, paramsMap);
		}
		return BanksUtil.paginatedResponse(bankDetails, paramsMap);
	}

	/**
	 * Do filtering in the list on the query fields
	 * @param bankDetails
	 * @param queryParamMap
	 * @return
	 */
	private static List<BankDetails> filterBanksResponse(List<BankDetails> bankDetails,
			Map<String, String> queryParamMap) {
		List<Predicate<BankDetails>> allPredicates = getCommonPredicates(
				queryParamMap);
		if (queryParamMap.containsKey("product")) {
			allPredicates.add(bank -> bank.getProducts().contains(queryParamMap.get("product")));
		}
		return bankDetails.stream().filter(allPredicates.stream().reduce(x -> true, Predicate::and))
				.collect(Collectors.toList());
	}

	private static BankDetails staticBanksMapper(BankModel bankModel) {
		BankDetails bankDetails = new BankDetails();
		bankDetails.setName(bankModel.getName());
		bankDetails.setId(bankModel.getBic());
		bankDetails.setCountryCode(bankModel.getCountryCode());
		bankDetails.setProducts(bankModel.getProducts());
		return bankDetails;
	}

}
