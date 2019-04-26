# API server


### Test rest-calls


**Fetch a login token**
```bash
export GARSSON_API_TOKEN=$(curl -X POST localhost:8080/api/v1/login -d '{"email": "chris@hesters.nl", "password":"234"}' -s | jq -r .token)
```

**Fetch orders**
```bash
curl localhost:8080/api/v1/orders -H "Authorization: Bearer $GARSSON_API_TOKEN" -s | python -m json.tool
```