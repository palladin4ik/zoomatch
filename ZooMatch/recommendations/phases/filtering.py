from datetime import date, timedelta


def apply_soft_filters(candidates, params):
    requires_pedigree = params.get('requires_pedigree', False)
    min_age = params.get('min_age', None)
    max_age = params.get('max_age', None)
    max_months_since_mating = params.get('max_months_since_mating', None)

    result = []

    for pet in candidates:
        if requires_pedigree and not pet.has_pedigree:
            continue

        if min_age is not None and pet.age < min_age:
            continue

        if max_age is not None and pet.age > max_age:
            continue

        if max_months_since_mating is not None and pet.last_mating_date:
            limit = date.today() - timedelta(
                days=max_months_since_mating * 30
            )
            if pet.last_mating_date < limit:
                continue

        result.append(pet)

    return result
