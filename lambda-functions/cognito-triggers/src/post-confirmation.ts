import { CognitoUserPoolTriggerEvent, CognitoUserPoolTriggerHandler } from 'aws-lambda';
import axios from 'axios';

/**
 * Cognito Post-Confirmation Lambda Trigger
 *
 * Triggered after a user successfully confirms their email address.
 * Creates or updates the user in the backend database via REST API.
 *
 * Environment Variables:
 * - API_BASE_URL: The backend API base URL (e.g., https://api.example.com)
 * - API_SECRET: Secret for authenticating Lambda to backend (stored in Secrets Manager)
 */

interface UserCreationRequest {
  cognitoUserId: string;
  email: string;
  name: string;
}

interface ApiConfig {
  baseUrl: string;
  secret: string;
}

/**
 * Get API configuration from environment variables.
 */
function getApiConfig(): ApiConfig {
  const baseUrl = process.env.API_BASE_URL;
  const secret = process.env.API_SECRET;

  if (!baseUrl || !secret) {
    throw new Error('Missing required environment variables: API_BASE_URL, API_SECRET');
  }

  return { baseUrl, secret };
}

/**
 * Create or update user in backend database.
 */
async function createUserInBackend(userRequest: UserCreationRequest, config: ApiConfig): Promise<void> {
  try {
    console.log(`Creating user in backend: ${userRequest.email}`);

    const response = await axios.post(
      `${config.baseUrl}/api/internal/users/from-cognito`,
      userRequest,
      {
        headers: {
          'Content-Type': 'application/json',
          'X-API-Secret': config.secret, // Internal API authentication
        },
        timeout: 10000, // 10 second timeout
      }
    );

    console.log(`User created successfully: ${response.data.id}`);
  } catch (error) {
    if (axios.isAxiosError(error)) {
      console.error('API error creating user:', {
        status: error.response?.status,
        data: error.response?.data,
        message: error.message,
      });
    } else {
      console.error('Unexpected error creating user:', error);
    }
    throw error;
  }
}

/**
 * Handler for Cognito Post-Confirmation trigger.
 */
export const handler: CognitoUserPoolTriggerHandler = async (
  event: CognitoUserPoolTriggerEvent
): Promise<CognitoUserPoolTriggerEvent> => {
  console.log('Post-confirmation trigger invoked:', JSON.stringify(event, null, 2));

  try {
    const { userPoolId, userName, request } = event;
    const { userAttributes } = request;

    // Extract user information from Cognito event
    const cognitoUserId = userName; // This is the 'sub' claim
    const email = userAttributes.email;
    const name = userAttributes.name || userAttributes.email.split('@')[0];

    console.log(`Processing confirmation for user: ${email} (${cognitoUserId})`);

    // Get API configuration
    const config = getApiConfig();

    // Create user in backend database
    const userRequest: UserCreationRequest = {
      cognitoUserId,
      email,
      name,
    };

    await createUserInBackend(userRequest, config);

    console.log(`Post-confirmation completed successfully for: ${email}`);

    // Return the event to Cognito (required)
    return event;
  } catch (error) {
    console.error('Error in post-confirmation handler:', error);

    // Log error but don't fail the confirmation
    // This ensures users can still sign in even if backend sync fails
    // The user will be created on first login via JWT extraction
    console.warn('Failed to sync user to backend, will retry on first login');

    return event;
  }
};
