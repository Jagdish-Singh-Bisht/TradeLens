import { useBrokerAccount } from '../context/BrokerAccountContext'
import './GlobalBrokerAccountSelector.css'

function GlobalBrokerAccountSelector() {

    const {
        accounts,
        selectedBrokerAccountId,
        selectBrokerAccount,
        loading,
        error
    } = useBrokerAccount();

    return (
        <div className="global-account-selector">

            <label htmlFor="globalBrokerAccount">
                Broker Account
            </label>

            { loading ? (
                <span>Loading accounts...</span>
            ) : error ? (
                <span className="account-selector-error">
                    {error}
                </span>
            ) : accounts.length === 0 ? (
                <span>No broker accounts available</span>
            ): (
                <select

                    id="globalBrokerAccount"
                    value={selectedBrokerAccountId}
                    onChange={event =>
                        selectBrokerAccount(event.target.value)
                    }
                >
                    <option value="ALL">
                         All Accounts
                    </option>

                    { accounts.map(account => (
                        <option
                            key={account.id}
                            value={String(account.id)}
                        >
                            {account.brokerName} - {account.accountName} (ID : {account.id})
                        </option>
                    ))}

                </select>
            )}

        </div>
    );
}

export default GlobalBrokerAccountSelector;