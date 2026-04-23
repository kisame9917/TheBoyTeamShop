package com.vestshop.Service;

import com.vestshop.dto.request.ClientOrderCancelRequest;
import com.vestshop.dto.request.ClientOrderUpdateItemsRequest;
import com.vestshop.dto.request.ClientOrderUpdateShippingRequest;
import com.vestshop.dto.response.OnlineOrderLookupResponse;

public interface ClientOrderMutationService {

    OnlineOrderLookupResponse cancelMyOrder(Long orderId,
                                            String principal,
                                            ClientOrderCancelRequest request);

    OnlineOrderLookupResponse updateMyOrderShipping(Long orderId,
                                                    String principal,
                                                    ClientOrderUpdateShippingRequest request);
    OnlineOrderLookupResponse updateMyOrderItems(Long orderId,
                                                 String principal,
                                                 ClientOrderUpdateItemsRequest request);
}