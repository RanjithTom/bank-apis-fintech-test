package io.bankbridge.provider;

import java.util.List;

public interface RemoteBanksProvider {

  /**
   * Method for getting the remote bank details.
   * Created this as a interface if we are following multiple implementations for getting remote bank details
   * I have done the implementation by using parallel call. If needed we can implement serial call mechanism also.
   * @param uris
   * @return
   */
  Object getRemoteBanksDetails(List<String> uris);
}
