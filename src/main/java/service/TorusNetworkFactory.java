package service;

public interface TorusNetworkFactory {
    TorusNetworkService createNetwork(int rows, int cols, int[] ids);
}
