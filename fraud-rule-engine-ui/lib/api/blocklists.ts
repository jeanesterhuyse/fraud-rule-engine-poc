import { apiClient } from './client';
import {
  BlockedCustomer,
  BlockedMerchant,
  BlockCustomerRequest,
  BlockMerchantRequest,
} from '@/types/blocklist';

export const blocklistApi = {
  // Blocked Customers
  getBlockedCustomers: async (): Promise<BlockedCustomer[]> => {
    const response = await apiClient.get('/blocklists/customers');
    return response.data;
  },

  blockCustomer: async (request: BlockCustomerRequest): Promise<BlockedCustomer> => {
    const response = await apiClient.post('/blocklists/customers', request);
    return response.data;
  },

  unblockCustomer: async (customerId: string): Promise<void> => {
    await apiClient.delete(`/blocklists/customers/${customerId}`);
  },

  // Blocked Merchants
  getBlockedMerchants: async (): Promise<BlockedMerchant[]> => {
    const response = await apiClient.get('/blocklists/merchants');
    return response.data;
  },

  blockMerchant: async (request: BlockMerchantRequest): Promise<BlockedMerchant> => {
    const response = await apiClient.post('/blocklists/merchants', request);
    return response.data;
  },

  unblockMerchant: async (merchantName: string): Promise<void> => {
    await apiClient.delete(`/blocklists/merchants/${encodeURIComponent(merchantName)}`);
  },
};
