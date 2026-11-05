import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState
} from 'react';

const BrokerAccountContext = createContext(null);

const STORAGE_KEY = 'tradelens.selectedBrokerAccountId';

const ALL_ACCOUNTS = 'ALL';

export function BrokerAccountProvider({ children }) {

    const [accounts, setAccounts] = useState([]);
    const [selectedBrokerAccountId, setSelectedBrokerAccountId] = useState('');
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const refreshAccounts = useCallback(async () => {
        try {
            setLoading(true);
            setError(null);

            const response = await fetch(
                'http://localhost:8080/api/broker-accounts',
                {
                    credentials: 'include'
                }
            );

            if(!response.ok) {
                throw new Error(
                    `Failed to load broker accounts: HTTP ${response.status}`
                );
            }

            const result = await response.json();

            if(!Array.isArray(result)) {
                throw new Error('Broker accounts API did not return a list.');
            }

            setAccounts(result);

            if(result.length === 0) {
                setSelectedBrokerAccountId('');
                localStorage.removeItem(STORAGE_KEY);
                return;
            }

            const savedAccountId = localStorage.getItem(STORAGE_KEY);

            const existingAccount = result.find(
                account => String(account.id) === String(savedAccountId)
            );

            const accountId =
                savedAccountId === ALL_ACCOUNTS
                    ? ALL_ACCOUNTS
                    : String(existingAccount ? existingAccount.id : result[0].id);

            setSelectedBrokerAccountId(accountId);
            localStorage.setItem(STORAGE_KEY, accountId);

        } catch(err) {
            console.error('Error loading broker accounts: ', err);
            setError(err.message || 'Unable to load broker accounts.');
        } finally {
            setLoading(false);
        }

    }, []);

    const selectBrokerAccount = useCallback((accountId) => {

        if(accountId !== ALL_ACCOUNTS &&
            !accounts.some(account => String(account.id) === String(accountId))) {

            return;
        }

        const normalizedId = String(accountId);

        setSelectedBrokerAccountId(normalizedId);
        localStorage.setItem(STORAGE_KEY, normalizedId);

    }, [accounts]);

    useEffect(() => {
        refreshAccounts();
    }, [refreshAccounts]);

    const contextValue = useMemo(() => ({
        accounts,
        selectedBrokerAccountId,
        selectBrokerAccount,
        loading,
        error,
        refreshAccounts
    }), [
        accounts,
        selectedBrokerAccountId,
        selectBrokerAccount,
        loading,
        error,
        refreshAccounts
    ]);

    return (
        <BrokerAccountContext.Provider value={contextValue}>
            {children}
        </BrokerAccountContext.Provider>
    );

}

export function useBrokerAccount() {

    const context = useContext(BrokerAccountContext);

    if(!context) {
        throw new Error(
            'useBrokerAccount must be used inside BrokerAccountProvider.'
        );
    }

    return context;
}