package io.collective.start;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.collective.articles.ArticleDataGateway;
import io.collective.articles.ArticleRecord;
import io.collective.articles.ArticlesController;
import io.collective.endpoints.EndpointDataGateway;
import io.collective.endpoints.EndpointTask;
import io.collective.endpoints.EndpointWorkFinder;
import io.collective.endpoints.EndpointWorker;
import io.collective.restsupport.BasicApp;
import io.collective.restsupport.NoopController;
import io.collective.restsupport.RestTemplate;
import io.collective.workflow.WorkScheduler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class App extends BasicApp {
    private ArticleDataGateway dataGateway;

    @Override
    public void start() {
        super.start();

        { // todo - start the endpoint worker
            EndpointWorker worker = new EndpointWorker(new RestTemplate(), dataGateway);
            WorkScheduler<EndpointTask> scheduler = new WorkScheduler<>(new EndpointWorkFinder(new EndpointDataGateway()),
                    Collections.singletonList(worker), 30);
            scheduler.start();
        }
    }
    public App(int port) {
        super(port);
    }

    @NotNull
    @Override
    protected HandlerList handlerList() {
        dataGateway = new ArticleDataGateway(List.of(
                new ArticleRecord(10101, "Programming Languages InfoQ Trends Report - October 2019 4", true),
                new ArticleRecord(10106, "Ryan Kitchens on Learning from Incidents at Netflix, the Role of SRE, and Sociotechnical Systems", true)
        ));

        HandlerList list = new HandlerList();
        list.addHandler(new ArticlesController(new ObjectMapper(), dataGateway));
        list.addHandler(new NoopController());
        return list;
    }

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        String port = System.getenv("PORT") != null ? System.getenv("PORT") : "8001";
        App app = new App(Integer.parseInt(port));
        app.start();
    }
}
