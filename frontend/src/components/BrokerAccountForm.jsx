
import { useState } from 'react';

function BrokerAccountForm({ onCreated }) {
    const [brokerName, setBrokerName] = useState('Zerodha');
    const [accountName, setAccountName] = useState('');
    const [error, setError] = useState('');
    const [saving, setSaving] = useState(false);

    async function handleSubmit(event) {
        event.preventDefault();
        setError('');
        setSaving(true);

        try {
            // Get the CSRF token required by Spring Security
            const csrfResponse = await fetch(
                'http://localhost:8080/api/auth/csrf',
                { credentials: 'include' }
            );

            if (!csrfResponse.ok) {
                throw new Error('Could not get security token. Please log in again.');
            }

            const csrfData = await csrfResponse.json();

            // Create the account for the logged-in user
            const response = await fetch(
                'http://localhost:8080/api/broker-accounts',
                {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-XSRF-TOKEN': csrfData.token
                    },
                    body: JSON.stringify({ brokerName, accountName })
                }
            );

            if (!response.ok) {
                const message = await response.text();
                throw new Error(message || `Request failed: HTTP ${response.status}`);
            }

            const createdAccount = await response.json();
            setAccountName('');
            onCreated(createdAccount);

        } catch (err) {
            console.error('Account creation failed:', err);
            setError(err.message || 'Could not create broker account.');
        } finally {
            setSaving(false);
        }
    }

    return (
        <form onSubmit={handleSubmit}>
            <h3>Create Broker Account</h3>

            <label>
                Broker
                <select
                    value={brokerName}
                    onChange={event => setBrokerName(event.target.value)}
                >
                    <option value="Zerodha">Zerodha</option>
                    <option value="Groww">Groww</option>
                    <option value="Upstox">Upstox</option>
                    <option value="Other">Other</option>
                </select>
            </label>

            <label>
                Account name
                <input
                    value={accountName}
                    onChange={event => setAccountName(event.target.value)}
                    placeholder="e.g. My Trading Account"
                    required
                />
            </label>

            <button type="submit" disabled={saving}>
                {saving ? 'Creating...' : 'Create Account'}
            </button>

            {error && <p role="alert">{error}</p>}
        </form>
    );
}

export default BrokerAccountForm;
