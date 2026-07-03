package service.impl;

import jakarta.enterprise.context.ApplicationScoped;
import service.TorusNetworkFactory;
import service.TorusNetworkService;

@ApplicationScoped
public class DefaultTorusNetworkFactory implements TorusNetworkFactory {
    @Override
    public TorusNetworkService createNetwork(int rows, int cols, int[] ids) {
        return new TorusNetwork(rows, cols, ids);
    }
}
