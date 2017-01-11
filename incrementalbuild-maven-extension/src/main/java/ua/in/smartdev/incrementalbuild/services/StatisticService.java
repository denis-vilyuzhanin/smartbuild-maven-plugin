package ua.in.smartdev.incrementalbuild.services;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;

import rx.Observable;
import ua.in.smartdev.incrementalbuild.model.IncrementalBuildStatistic;
import ua.in.smartdev.incrementalbuild.model.ProjectStateUpdateResult;

@Component(role = StatisticService.class)
public class StatisticService {

	
	public Observable<IncrementalBuildStatistic> buildStatistic(
			List<? extends ProjectStateUpdateResult> allProjects) {
		IncrementalBuildStatistic statistic = new IncrementalBuildStatistic();
		statistic.setTotalBuildDuration(allProjects.size() * 1000);
		return Observable.just(new IncrementalBuildStatistic());
	}
}
