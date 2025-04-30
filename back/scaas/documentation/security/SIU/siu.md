This document describes the API to use to manage B2B identity

The B2B IAM is composed of 3 applications :
- SIU (Basicat SIU) : LDAP database that manages identities and identity federation
- 54Y (Basicat 54Y): Application that manages the interaction with SIU LDAP database ( API + Web)
- Cassiopee (Basicat DRO): Application used to authenticate users (Web + cookies + redirection + email to set/reset password)
  Currenlty the API from 54Y are exposed ton IOS-W

The API documentation can be found on Orange developer Inside : [ODI API electronicidentityb2b](https://developer-inside.sso.infra.ftgroup/apis/electronicidentityb2b/)

2 roles will be available for Smarchain
- smartchain.admin
- smartchain.user
  Only identities with these roles will be authorized to connect on the smarchain front web.


# 1- Search or list information related to an identiy

## 1.1 Global Search on any parameter
```json
GET /searches
{
    "searchElectronicIdentity": {
        "searchIndividual": {
            "individualNames": {
                "givenName": "$firstName",
                "familyName": "$lastName"
            }
        },
        "organization": {
            "organizationIdentifications": [
                {
                    "type": "Ideta",
                    "id": "$ideta"
                }
            ]
        }        
        "email": {
            "emailAddress": "$emailAddress"
        }
    }
}
```

The result is provided with a list of identities.

###  WARNING : error if too many reults
An error response may be received in case there are too many results.
// TODO : check if there is no evolution with pagination

```json
{
    "code": 6,
    "message": "Too many account found",
    "description": "Too many account found",
    "infoURL": null,
    "details": []
}
```

## 1.2 Basic search based on Login
```json
GET /electronicIdentities?login={{login}}
```

## 1.3 display information related to an identity
```json
GET /electronicIdentities/{{uid}}
```

### Example of reponse
```json
{
    "language": "fr",
    "electronicIdentityStatus": {
        "status": "Active"
    },
    "rightContainer": null,
    "uid": "1151206251985115",
    "individual": {
        "title": null,
        "givenName": "Nicolas",
        "familyName": "Billy",
        "interlocutorId": null,
        "riceId": ""
    },
    "email": {
        "emailAddress": "nicolas.billy@orange.com"
    },
    "organization": {
        "organizationIdentifications": [
            {
                "type": "Ideta",
                "id": "10120563"
            },
            {
                "type": "Siret",
                "id": "38012986646553"
            },
            {
                "type": "Ident",
                "id": "00292906"
            },
            {
                "type": "Siren",
                "id": "380129866"
            }
        ]
    },
    "marketSegment": "Enterprise",
    "telephoneNumbers": [
        {
            "type": "LandLine",
            "number": "+33123456789"
        }
    ],
    "isMainAdministrator": null,
    "credential": {
        "login": "nicolas.billy@ucc.teaming.fr"
    },
    "partyCreatorID": "admcptbe",
    "partyCreationDate": "2017-11-30T18:21:59.000+01:00",
    "local_LastConnexionDate": "2023-07-17T07:36:36.000+02:00",
    "ERDCode": [
        ""
    ]
}
```

# 2- Create an identiy

```json
POST /electronicIdentities
{
    "language": "fr",
    "rightContainer": {
        "id": "$role"  // in case several role shall be provided, it is possible to define a specific value that includes several roles
    },
    "individual": {
        "givenName": "$firstName",
        "familyName": "$lastName"
    },
    "email": {
        "emailAddress": "$emailAddress"
    },
    "organization": {
        "organizationIdentifications": [
            {
                "type": "Ideta",
                "id": "$ideta"
            }
        ]
    },
    "telephoneNumbers": [
        {
            "type": "LandLine",      // either "LandLine" (+33[1-5][0-9]8) or "Mobile" (+33[6-7][0-9]8)
            "number": "$phoneNumber"
        }
    ],
    "credential": {         // this section is optional and defines only a wish
        "login": "$login"  
    }
}
```

IMPORTANT:
- Parameter "X-REQUESTOR-ID" shall be defined with Smarchain basicat
- In order to send an email with the identity createdn the following parameters shall be sent :
    - passwordCreateEmailboolean (header)
      If true, if the account is created (http code return 201), the method can send the password create email to the user. No email sent otherwise
      Default value : false
    - passwordCreateEmailContextCode (header)
      If passwordCreateEmail is set to true, you can indicate a context code.
      You need an Interface Contract with Cassiopée. The context code enables the customisation of the email.


# 3 Manage the role associated to an identity

## 3.1 Check the role associated to an identity
```json
GET /electronicIdentities/{{uid}}/webApplicationRoles
```

### Example of response
```json
[
    {
        "name": "WEFT"
    },
    {
        "name": "ITT.inventory"
    },
    {
        "name": "ITT.PEX"
    },
    {
        "name": "smile.default"
    },
    {
        "name": "smile.migration"
    },
    {
        "name": "smile.customer_requester"
    },
    {
        "name": "smile.view.mysupport"
    },
    {
        "name": "itt.indigo.requestor"
    },
    {
        "name": "itt.indigo.orderapprover"
    },
    {
        "name": "0RQ.MDEP"
    },
    {
        "name": "FED_ID"
    },
    {
        "name": "OIDC.scopes"
    },
    {
        "name": "ARD.visible"
    },
    {
        "name": "ITT.DEFAUT"
    },
    {
        "name": "ITT.DBVOIP"
    },
    {
        "name": "ITT.ECSDB"
    },
    {
        "name": "ITT._ORCH"
    },
    {
        "name": "ITT.SUIVIS"
    },
    {
        "name": "ITT.UMT"
    },
    {
        "name": "ITT.WATOO"
    },
    {
        "name": "ITT.PERSPECTIV"
    },
    {
        "name": "ITT.WISCONSIN"
    },
    {
        "name": "cty.user"
    }
]
```

## 3.2 Modify the role associated to an identity

First remove the previous role
```json
DELETE /electronicIdentities/{{uid}}/webApplicationRoles/{{oldRole}}
```

Then add the new role
```json
POST /electronicIdentities/{{uid}}/webApplicationRoles/{{newRole}}
```

# 4 Delete an identity

It is not possible to delete an identity from smartchain backend as the identity may be shared by several applications.
In case an identity shall be removed, only roles shall be removes.
In this case, 2 solutions are possible

Either perform a GET /webApplicationRoles to check the existing smartchain role(s) and remove them

```json
GET /electronicIdentities/{{uid}}/webApplicationRoles/

DELETE /electronicIdentities/{{uid}}/webApplicationRoles/$oldRole
```

Or, always remove all roles associated to the identities
```json
DELETE /electronicIdentities/{{uid}}/webApplicationRoles/smartchain.user
DELETE /electronicIdentities/{{uid}}/webApplicationRoles/smarchain.admin
```
